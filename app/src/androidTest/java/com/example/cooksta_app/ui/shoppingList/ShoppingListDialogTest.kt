package com.example.cooksta_app.ui.shoppingList

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cooksta_app.R
import com.example.cooksta_app.network.RetrofitClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoppingListDialogTest {

    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        every { viewModel.clear() } returns Unit

        mockWebServer = MockWebServer()
        mockWebServer.start(8080)
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse()
                    .setResponseCode(404)
                    .setBody("{\"message\": \"Not found\"}")
        }

        RetrofitClient.useMockServerUrl(mockWebServer.url("/api/"))
    }

    @Test
    fun shoppingList_delete_dialog_negative_calls_clear() {
        launchFragmentInContainer<ShoppingListFragment>(themeResId = R.style.Theme_CookstaApp).onFragment { fragment ->
            val viewModelProperty = ShoppingListFragment::class.java.getDeclaredField("viewModel")
            viewModelProperty.isAccessible = true
            viewModelProperty.set(fragment, viewModel)
        }

        onView(ViewMatchers.withId(R.id.deleteShoppingList)).perform(click())

        onView(ViewMatchers.withText(R.string.delete))
            .inRoot(isDialog())
            .perform(click())

        verify { viewModel.clear() }
    }

    @Test
    fun shoppingList_delete_dialog_positive_does_not_call_clear() {
        launchFragmentInContainer<ShoppingListFragment>(themeResId = R.style.Theme_CookstaApp).onFragment { fragment ->
            val viewModelProperty = ShoppingListFragment::class.java.getDeclaredField("viewModel")
            viewModelProperty.isAccessible = true
            viewModelProperty.set(fragment, viewModel)
        }

        onView(ViewMatchers.withId(R.id.deleteShoppingList)).perform(click())

        onView(ViewMatchers.withText(R.string.no))
            .inRoot(isDialog())
            .perform(click())

        verify(exactly = 0) { viewModel.clear() }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}