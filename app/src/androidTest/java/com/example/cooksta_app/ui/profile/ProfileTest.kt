package com.example.cooksta_app.ui.profile

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cooksta_app.R
import com.example.cooksta_app.network.RetrofitClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ProfileTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var latch: CountDownLatch

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        RetrofitClient.useMockServerUrl(mockWebServer.url("/api/"))
    }

    @Test
    fun loadProfile_displaysCorrectData() {
        latch = CountDownLatch(2)

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/api/users/me" -> MockResponse()
                        .setResponseCode(200)
                        .setBody("""
                            {
                                "id": "u12345678",
                                "username": "TestUser",
                                "biography": "Food lover"
                            }
                        """.trimIndent())
                        .also {
                            latch.countDown()
                        }

                    "/api/users/me/recipes" -> MockResponse()
                        .setResponseCode(200)
                        .setBody("""
                            [
                                {
                                    "id": "r12345678",
                                    "creatorId": "u12345678",
                                    "title": "TestRecipe",
                                    "description": "Test description",
                                    "ingredients": {
                                        "TestIngredient": "100 ml"
                                    },
                                    "portions": 1,
                                    "categories": ["main"],
                                    "isPrivate": false
                                }
                            ]
                        """.trimIndent())
                        .also {
                            latch.countDown()
                        }

                    else -> MockResponse()
                        .setResponseCode(404)
                        .setBody("{\"message\": \"Not found\"}")
                }
            }
        }

        val scenario = launchFragmentInContainer<ProfileFragment>(themeResId = R.style.Theme_CookstaApp)

        latch.await(5, TimeUnit.SECONDS)

        onView(withId(R.id.username)).check(matches(withText("TestUser")))
        onView(withId(R.id.biography)).check(matches(withText("Food lover")))

        scenario.close()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
