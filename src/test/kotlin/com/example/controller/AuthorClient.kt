package com.example.controller

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single


@Client("/author")
interface AuthorClient {
    @Get("/")
    fun index(): Single<String>
}
