/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.appconfig;

import static app.coronawarn.server.services.distribution.common.Helpers.loadApplicationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedDailySummariesConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedDiagnosisKeysDataMapping;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedExposureConfiguration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class YamlLoaderTest {

  @Test
  void okFile() throws UnableToLoadFileException {
    var result = loadApplicationConfiguration("configtests/app-config_ok.yaml");
    assertThat(result).withFailMessage("File is null, indicating loading failed").isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "configtests/app-config_empty.yaml",
      "configtests/wrong_file.yaml",
      "configtests/app-config_broken_syntax.yaml",
      "configtests/naming_mismatch.yaml",
      "configtests/file_does_not_exist_anywhere.yaml"
  })
  void throwsLoadFailure(String fileName) {
    assertThatExceptionOfType(UnableToLoadFileException.class).isThrownBy(() -> loadApplicationConfiguration(fileName));
  }

  @ParameterizedTest
  @MethodSource("getYamlDeserializationExpectations")
  <T> void successWithDeserializingIntoClass(String yamlPath, Class<T> expectedClassType) throws UnableToLoadFileException {
    var result = YamlLoader.loadYamlIntoClass(yamlPath, expectedClassType);
    assertThat(result).isNotNull();
  }

  @Test
  void failWhenYamlNotAlligned() {
    assertThatExceptionOfType(UnableToLoadFileException.class).isThrownBy(() -> {
      YamlLoader.loadYamlIntoClass("configtests/daily-summaries-config-fail.yaml", DeserializedDailySummariesConfig.class);
    });
    assertThatExceptionOfType(UnableToLoadFileException.class).isThrownBy(() -> {
      YamlLoader.loadYamlIntoClass("configtests/not-existing.yaml", DeserializedDailySummariesConfig.class);
    });
  }

  private static Stream<Arguments> getYamlDeserializationExpectations() {
    return Stream.of(
        Arguments.of("configtests/daily-summaries-config-ok.yaml", DeserializedDailySummariesConfig.class),
        Arguments.of("configtests/exposure-configuration-v2-ok.yaml", DeserializedExposureConfiguration.class),
        Arguments.of("configtests/diagnosis-keys-data-mapping-ok.yaml", DeserializedDiagnosisKeysDataMapping.class)
    );
  }
}
