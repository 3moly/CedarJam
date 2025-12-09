package com.moly3.cedarjam.core.net

import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileStructure
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.error
import com.moly3.cedarjam.core.domain.model.success
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.forms.*
import io.ktor.client.request.get
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class RemoteSyncRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val json: Json
) : IRemoteSyncRepository {

    override suspend fun upload(
        userName: String,
        workspaceName: String,
        archiveByteArray: ByteArray,
        metadata: List<FileMetadata>,
        filesToDownload: List<String>
    ): ResultWrapper<ByteArray, String> {
        return try {

            val metadataJson = json.encodeToString(metadata)
            val filesToDownload = json.encodeToString(filesToDownload)
            val response: HttpResponse = httpClient.submitFormWithBinaryData(
                url = "${baseUrl}api/workspaces/upload",
                formData = formData {
                    append("userName", userName)
                    append("workspaceName", workspaceName)
                    append("metadata", metadataJson)
                    append("filesToDownload", filesToDownload)
                    append("archive", archiveByteArray, Headers.build {
                        append(HttpHeaders.ContentType, "application/zip")
                        append(HttpHeaders.ContentDisposition, "filename=\"archive.zip\"")
                    })
                }
            )
            if (response.status.isSuccess()) {
                success(response.bodyAsBytes())
            } else {
                var sd = response.bodyAsText()
                error("Upload failed with status: ${response.status.value} error: ${sd}")
            }
        } catch (e: Exception) {
            error("Upload error: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun workspaceFiles(
        userName: String,
        workspaceName: String
    ): ResultWrapper<FileStructure, String> {
        return try {
            val response: HttpResponse =
                httpClient.get("${baseUrl}api/workspaces/$userName/${workspaceName}/files")

            if (response.status.isSuccess()) {
                val fileStructure: FileStructure = response.body()
                val fileStructurePrepared = if (getPlatform() == Platform.Android) {
                    fileStructure.copy(files = fileStructure.files.map {
                        val asd = (it.modifiedTime / 1000) * 1000
                        it.copy(modifiedTime = asd)
                    })
                } else {
                    fileStructure
                }
                success(fileStructurePrepared.copy(files = fileStructurePrepared.files.map { d ->
                    d.copy(
                        relativePath = d.relativePath
                    )
                }))
            } else {
                error("Upload failed with status: ${response.status.value}")
            }
        } catch (e: Exception) {
            error("Upload error: ${e.message ?: "Unknown error"}")
        }
    }
}