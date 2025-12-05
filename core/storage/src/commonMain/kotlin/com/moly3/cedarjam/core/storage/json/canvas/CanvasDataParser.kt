package com.moly3.cedarjam.core.storage.json.canvas

import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.canvas.CanvasShapeError
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors
import com.moly3.cedarjam.core.domain.model.canvas.Position
import com.moly3.cedarjam.core.domain.model.canvas.ShapeImpl
import com.moly3.cedarjam.core.domain.model.canvas.Size
import com.moly3.cedarjam.core.domain.func.ComposeColorSerializer
import com.moly3.cedarjam.core.storage.json.canvas.CanvasDataParser.ArcConnectionJson.Companion.toJson
import com.moly3.cedarjam.core.storage.json.canvas.CanvasDataParser.ArcConnectionJson.Companion.toModel
import com.moly3.dataviz.core.whiteboard.model.BoxSide
import com.moly3.dataviz.core.whiteboard.model.ShapeConnection
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object CanvasDataParser {
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }


    fun parse(jsonString: String): CanvasDataWithErrors {
        val rootElement = json.parseToJsonElement(jsonString).jsonObject

        val shapes = parseShapes(rootElement["shapes"]?.jsonArray ?: JsonArray(emptyList()))
        val connections =
            parseConnections(rootElement["connections"]?.jsonArray ?: JsonArray(emptyList()))

        return CanvasDataWithErrors(shapes, connections)
    }

    private fun parseShapes(shapesArray: JsonArray): List<ResultWrapper<ShapeImpl, CanvasShapeError>> {
        return shapesArray.map { shapeElement ->
            try {
                val shapeJson = json.decodeFromJsonElement<ShapeImpl>(shapeElement)
                ResultWrapper.Success(shapeJson)
            } catch (e: Exception) {
                val position = tryExtractPosition(shapeElement)
                val size = tryExtractSize(shapeElement)
                ResultWrapper.Error(
                    CanvasShapeError(
                        rawJson = shapeElement,
                        error = e.message ?: "Unknown error",
                        position = position ?: Position(0.0f, 0.0f),
                        size = size ?: Size(50.0f, 50.0f),
                        id = tryExtractId(shapeElement)
                    )
                )
            }
        }
    }

    private fun tryExtractId(element: JsonElement): Long {
        val posObj = element.jsonObject["position"]?.jsonObject ?: return 0L
        return posObj["id"]?.jsonPrimitive?.long ?: 0L
    }

    private fun tryExtractPosition(element: JsonElement): Position? {
        return try {
            val posObj = element.jsonObject["position"]?.jsonObject ?: return null
            Position(
                x = posObj["x"]?.jsonPrimitive?.floatOrNull ?: 0.0f,
                y = posObj["y"]?.jsonPrimitive?.floatOrNull ?: 0.0f
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun tryExtractSize(element: JsonElement): Size? {
        return try {
            val sizeObj = element.jsonObject["size"]?.jsonObject ?: return null
            Size(
                x = sizeObj["x"]?.jsonPrimitive?.floatOrNull ?: 100.0f,
                y = sizeObj["y"]?.jsonPrimitive?.floatOrNull ?: 100.0f
            )
        } catch (e: Exception) {
            null
        }
    }

    @Serializable
    data class ArcConnectionJson(
        val id: Long,
        val fromBox: Long,
        val toBox: Long,
        var fromSide: BoxSide,
        var toSide: BoxSide,
        val arcHeight: Float = 80f,
        @Serializable(with = ComposeColorSerializer::class)
        val color: Color?
    ) {
        companion object {
            fun ShapeConnection<Long>.toJson(): ArcConnectionJson {
                return ArcConnectionJson(
                    id = id,
                    fromBox = fromBoxId,
                    toBox = toBoxId,
                    fromSide = fromSide,
                    toSide = toSide,
                    arcHeight = arcHeight,
                    color = color
                )
            }
            fun ArcConnectionJson.toModel(): ShapeConnection<Long> {
                return ShapeConnection(
                    id = id,
                    fromBoxId = fromBox,
                    toBoxId = toBox,
                    fromSide = fromSide,
                    toSide = toSide,
                    arcHeight = arcHeight,
                    color = color
                )
            }
        }
    }

    private fun parseConnections(connectionsArray: JsonArray): List<ResultWrapper<ShapeConnection<Long>, CanvasShapeError>> {
        return connectionsArray.map { connectionElement ->
            try {
                val arcConnectionJson =
                    json.decodeFromJsonElement<ArcConnectionJson>(connectionElement)

                ResultWrapper.Success(arcConnectionJson.toModel())
            } catch (e: Exception) {
                ResultWrapper.Error(
                    CanvasShapeError(
                        connectionElement,
                        e.message ?: "Unknown error",
                        position = Position(0.0f, 0.0f),
                        size = Size(0.0f, 0.0f),
                        id = 0L
                    )
                )
            }
        }
    }

    fun serialize(data: CanvasDataWithErrors): String {
        val shapesArray = buildJsonArray {
            data.shapes.forEach { result ->
                when (result) {
                    is ResultWrapper.Success -> add(json.encodeToJsonElement(result.value))
                    is ResultWrapper.Error -> add(result.error.rawJson)
                }
            }
        }

        val connectionsArray = buildJsonArray {
            data.connections.forEach { result ->
                when (result) {
                    is ResultWrapper.Success -> add(json.encodeToJsonElement(result.value.toJson()))
                    is ResultWrapper.Error -> add(result.error.rawJson)
                }
            }
        }

        val rootObject = buildJsonObject {
            put("shapes", shapesArray)
            put("connections", connectionsArray)
        }

        return json.encodeToString(JsonObject.serializer(), rootObject)
    }
}