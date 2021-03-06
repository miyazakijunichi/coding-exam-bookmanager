package com.example.controller

import com.example.domain.Author
import com.example.domain.AuthorRepository
import com.example.domain.BookRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.micronaut.views.View
import java.net.URI
import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException
import javax.validation.Valid


@Validated
@Controller("/author")
class AuthorController {

    val kListLimit: Int = 20

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var authorRepository: AuthorRepository

    @Transactional
    @Get("/")
    @View("author/index")
    fun index(@QueryValue("q") query: String?): HttpResponse<Map<String, Any>> {
        val authors = query?.let { authorRepository.searchByName(it) } ?: authorRepository.findAll().toList()

        return HttpResponse.ok(mapOf("authors" to authors, "query" to (query ?: "")))
    }

    @Transactional
    @Get("/list")
    fun listAll(): HttpResponse<Map<String, Any>> {
        return listHandler()
    }

    @Transactional
    @Get("/list/{query}")
    fun list(@PathVariable query: String): HttpResponse<Map<String, Any>> {
        return listHandler(query)
    }

    fun listHandler(query: String = ""): HttpResponse<Map<String, Any>> {
        val authors = authorRepository.searchByName(query, kListLimit)

        return HttpResponse.ok(mapOf("success" to true, "results" to authors.map {
            mapOf<String, Any>(
                    "name" to it.name,
                    "value" to it.id,
            )
        }))
    }

    @Transactional
    @Get("/edit")
    @View("author/edit")
    fun edit(@QueryValue("id") id: Long?): HttpResponse<Map<String, Any?>> {
        val author = if (id != null) {
            authorRepository.findById(id).orElse(null) ?: return HttpResponse.notFound()
        } else {
            Author()
        }

        return HttpResponse.ok(mapOf("author" to author, "id" to id))
    }

    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON)
    @Post("/edit")
    fun save(@Body @Valid authorForm: AuthorForm): HttpResponse<String> {
        val id = authorForm.id
        val author = if (id != null) {
            authorRepository.findById(id).orElse(null) ?: return HttpResponse.notFound("author not found")
        } else {
            Author()
        }

        authorRepository.save(author.apply {
            name = authorForm.name ?: ""
        })
        return HttpResponse.redirect(URI("/author"))
    }

    @View("author/edit")
    @Error(exception = ConstraintViolationException::class)
    fun onFailed(
            request: HttpRequest<Map<String, Any>>,
            ex: ConstraintViolationException
    ): HttpResponse<Map<String, Any?>> {
        val form = request.getBody(AuthorForm::class.java)
        val author = if (form.isPresent && form.get().id != null) {
            authorRepository.findById(form.get().id!!).orElse(null) ?: return HttpResponse.notFound()
        } else {
            Author()
        }

        return HttpResponse.ok(mapOf(
                "author" to author,
                "id" to form.get().id,
                "errors" to ex.constraintViolations.map { it.message },
        ))
    }

    class ExistsBooksOfAuthorException : RuntimeException()

    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON)
    @Post("/delete")
    fun delete(id: Long): HttpResponse<String> {
        if (bookRepository.countByAuthorId(id) > 0) {
            throw ExistsBooksOfAuthorException()
        }
        authorRepository.deleteById(id)
        return HttpResponse.redirect(URI("/author"))
    }

    @View("author/index")
    @Error(exception = ExistsBooksOfAuthorException::class)
    fun onExistsAuthorOfTheBook(
            request: HttpRequest<Map<String, Any>>,
            ex: ExistsBooksOfAuthorException
    ): HttpResponse<Map<String, Any>> {
        val authors = authorRepository.findAll().toList()

        return HttpResponse.ok(mapOf(
                "authors" to authors,
                "query" to "",
                "errors" to listOf("著者に紐づく書籍が登録されているため、削除できません")
        ))
    }
}
