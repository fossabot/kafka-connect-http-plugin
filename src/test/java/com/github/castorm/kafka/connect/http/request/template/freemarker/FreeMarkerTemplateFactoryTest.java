package com.github.castorm.kafka.connect.http.request.template.freemarker;

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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class FreeMarkerTemplateFactoryTest {

    FreeMarkerTemplateFactory factory = new FreeMarkerTemplateFactory();

    @Test
    void givenTemplate_whenApplyEmpty_thenAsIs() {
        assertThat(factory.create("template").apply(emptyMap())).isEqualTo("template");
    }

    @Test
    void givenTemplate_whenApplyValue_thenReplaced() {
        assertThat(factory.create("template ${key}").apply(ImmutableMap.of("key", "value"))).isEqualTo("template value");
    }
}
