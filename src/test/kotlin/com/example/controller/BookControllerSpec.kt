package com.example.controller

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class BookControllerSpec {

    @Inject
    lateinit var client: AuthorClient

    @Test
    fun testIndex_Get() {
        Assertions.assertTrue(client.index().blockingGet().contains("書籍一覧"))
    }
}

