package com.reon

import com.reon.config.ReonConfig
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SystemRoutesTest {
    @Test
    fun `health returns ok`() = testApplication {
        application { module(ReonConfig(port = 0)) }
        val res = client.get("/api/v1/health")
        assertEquals(HttpStatusCode.OK, res.status)
        assertContains(res.bodyAsText(), "\"status\":\"ok\"")
    }

    @Test
    fun `config returns version without api key`() = testApplication {
        application { module(ReonConfig(port = 0)) }
        val res = client.get("/api/v1/config")
        assertEquals(HttpStatusCode.OK, res.status)
        assertContains(res.bodyAsText(), "anonymousOnly")
    }
}
