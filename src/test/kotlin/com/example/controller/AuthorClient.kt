package com.example.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single


@Client("/author")
interface AuthorClient {
    @Get("/")
    fun index(): Single<HttpResponse<String>>

    @Get("/edit")
    fun edit(@QueryValue id: Long? = null): Single<HttpResponse<String>>

    @Post("/edit")
    fun save(name: String, id: Long? = null): Single<HttpResponse<String>>

    @Delete("/{id}")
    fun delete(id: Long): Single<HttpResponse<String>>
}
