/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.server.di

import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule


class ModelDataSerializerModule : AbstractModule() {
  @Provides
  @Singleton
  fun provideJson(): Json {
    return Json {
      serializersModule = SerializersModule {
        //polymorphic(WebSocketModelMessage::class) {
        //  WebSocketCompleteModelMessage::class with WebSocketCompleteModelMessage.serializer()
        //  WebSocketSingleModelChangeMessage::class with WebSocketSingleModelChangeMessage.serializer()
        //}
        //
        //polymorphic(HasModelId::class) {
        //  AgentData::class with AgentData.serializer()
        //  CustomerData::class with CustomerData.serializer()
        //  ProjectData::class with ProjectData.serializer()
        //  ProjectTemplateData::class with ProjectTemplateData.serializer()
        //  TaskData::class with TaskData.serializer()
        //  TaskSchemeData::class with TaskSchemeData.serializer()
        //}
      }
    }
  }
}
