package com.github.castorm.kafka.connect.http.response.jackson;

/*-
 * #%L
 * kafka-connect-http-plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import lombok.SneakyThrows;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class JacksonHttpResponseParser implements HttpResponseParser {

    private final Function<Map<String, ?>, JacksonHttpResponseParserConfig> configFactory;

    private final ObjectMapper mapper;

    private final JacksonHttpResponseItemMapper itemMapper;

    private JsonPointer itemsPointer;
    private Optional<JsonPointer> itemKeyPointer;
    private JsonPointer itemValuePointer;
    private Optional<JsonPointer> itemTimestampPointer;
    private Map<String, JsonPointer> itemOffsets;

    public JacksonHttpResponseParser() {
        this(JacksonHttpResponseParserConfig::new, new ObjectMapper(), new JacksonHttpResponseItemMapper());
    }

    JacksonHttpResponseParser(Function<Map<String, ?>, JacksonHttpResponseParserConfig> configFactory, ObjectMapper mapper, JacksonHttpResponseItemMapper itemMapper) {
        this.configFactory = configFactory;
        this.mapper = mapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        JacksonHttpResponseParserConfig config = configFactory.apply(configs);
        itemsPointer = config.getItemsPointer();
        itemKeyPointer = config.getItemKeyPointer();
        itemValuePointer = config.getItemValuePointer();
        itemTimestampPointer = config.getItemTimestampPointer();
        itemOffsets = config.getItemOffsets();
    }

    @Override
    public List<HttpResponseItem> parse(HttpResponse response) {

        JsonNode body = deserialize(response.getBody());

        return itemMapper.getItems(body, itemsPointer)
                .map(this::mapToItem)
                .collect(toList());
    }

    private HttpResponseItem mapToItem(JsonNode node) {

        return HttpResponseItem.builder()
                .key(itemKeyPointer.map(it ->itemMapper.getKey(node, it)).orElseGet(() -> randomUUID().toString()))
                .value(serialize(itemMapper.getValue(node, itemValuePointer)))
                .timestamp(itemTimestampPointer.map(it -> itemMapper.getTimestamp(node, it)).orElseGet(Instant.now()::toEpochMilli))
                .offset(itemMapper.getOffset(node, itemOffsets))
                .build();
    }

    @SneakyThrows(IOException.class)
    private JsonNode deserialize(byte[] body) {
        return mapper.readTree(body);
    }

    @SneakyThrows(IOException.class)
    private String serialize(JsonNode node) {
        return mapper.writeValueAsString(node);
    }
}
