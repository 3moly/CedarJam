package com.moly3.cedarjam.core.domain.service

interface IFileHasher {
    fun getFileHash(fullPath: String): String
}