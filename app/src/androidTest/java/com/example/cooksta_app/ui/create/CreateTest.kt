package com.example.cooksta_app.ui.create

import android.net.Uri
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cooksta_app.R
import com.example.cooksta_app.network.RetrofitClient
import com.google.android.material.textfield.TextInputLayout
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateTest {

    fun hasErrorTextById(id: Int): Matcher<View> {
        return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has error text from resource ID: $id")
            }

            override fun matchesSafely(item: TextInputLayout): Boolean {
                val errorText = item.error?.toString()
                val expectedErrorText = item.context.getString(id)
                return errorText == expectedErrorText
            }
        }
    }

    fun hasNoError(): Matcher<View> {
        return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has no error")
            }

            override fun matchesSafely(item: TextInputLayout): Boolean {
                return item.error == null
            }
        }
    }

    @Before
    fun setup() {
    }

    @Test
    fun createFragment_validation_fails_on_empty_title() {
        val scenario = launchFragmentInContainer<CreateFragment>(themeResId = R.style.Theme_CookstaApp)

        onView(withId(R.id.recipeDescriptionInput)).perform(typeText("Recipe description"))
        onView(withId(R.id.addIngredientButton)).perform(click())
        onView(withId(R.id.ingredientName))
            .check(matches(isDisplayed()))
            .perform(typeText("Test (ml)"))
        onView(withId(R.id.ingredientAmount))
            .check(matches(isDisplayed()))
            .perform(typeText("100"))
        onView(withId(R.id.categoryChipMain)).perform(click())
        scenario.withFragment {
            setSelectedImageUri(Uri.parse("content://someUri"))
        }

        Thread.sleep(1500)
        onView(withId(R.id.recipeSaveButton)).perform(scrollTo(), click())

        onView(withId(R.id.recipeTitleLayout)).check(matches(hasErrorTextById(R.string.error_title_required)))
    }

    @Test
    fun createFragment_validation_fails_on_empty_description() {
        val scenario = launchFragmentInContainer<CreateFragment>(themeResId = R.style.Theme_CookstaApp)

        onView(withId(R.id.recipeTitleInput)).perform(typeText("Test recipe title"))
        onView(withId(R.id.addIngredientButton)).perform(click())
        onView(withId(R.id.ingredientName))
            .check(matches(isDisplayed()))
            .perform(typeText("Test (ml)"))
        onView(withId(R.id.ingredientAmount))
            .check(matches(isDisplayed()))
            .perform(typeText("100"))
        onView(withId(R.id.categoryChipMain)).perform(click())
        scenario.withFragment {
            setSelectedImageUri(Uri.parse("content://someUri"))
        }

        Thread.sleep(1500)
        onView(withId(R.id.recipeSaveButton)).perform(scrollTo(), click())

        onView(withId(R.id.recipeDescriptionLayout)).check(matches(hasErrorTextById(R.string.error_description_required)))
    }

    @Test
    fun createFragment_validation_fails_on_empty_ingredient_selection() {
        val scenario = launchFragmentInContainer<CreateFragment>(themeResId = R.style.Theme_CookstaApp)

        onView(withId(R.id.recipeTitleInput)).perform(typeText("Test recipe title"))
        onView(withId(R.id.recipeDescriptionInput)).perform(typeText("Recipe description"))
        onView(withId(R.id.categoryChipMain)).perform(click())
        scenario.withFragment {
            setSelectedImageUri(Uri.parse("content://someUri"))
        }

        onView(withId(R.id.recipeSaveButton)).perform(scrollTo(), click())

        onView(withId(R.id.emptyIngredientSelectionView)).check(matches(isDisplayed()))
    }

    @Test
    fun createFragment_validation_fails_on_empty_category_selection() {
        val scenario = launchFragmentInContainer<CreateFragment>(themeResId = R.style.Theme_CookstaApp)

        onView(withId(R.id.recipeTitleInput)).perform(typeText("Test recipe title"))
        onView(withId(R.id.recipeDescriptionInput)).perform(typeText("Recipe description"))
        onView(withId(R.id.addIngredientButton)).perform(click())
        onView(withId(R.id.ingredientName))
            .check(matches(isDisplayed()))
            .perform(typeText("Test (ml)"))
        onView(withId(R.id.ingredientAmount))
            .check(matches(isDisplayed()))
            .perform(typeText("100"))
        scenario.withFragment {
            setSelectedImageUri(Uri.parse("content://someUri"))
        }

        Thread.sleep(1500)
        onView(withId(R.id.recipeSaveButton)).perform(scrollTo(), click())

        onView(withId(R.id.emptyCategorySelectionView)).check(matches(isDisplayed()))
    }

    @Test
    fun createFragment_validation_fails_on_empty_media_selection() {
        val scenario = launchFragmentInContainer<CreateFragment>(themeResId = R.style.Theme_CookstaApp)

        onView(withId(R.id.recipeTitleInput)).perform(typeText("Test recipe title"))
        onView(withId(R.id.recipeDescriptionInput)).perform(typeText("Recipe description"))
        onView(withId(R.id.addIngredientButton)).perform(click())
        onView(withId(R.id.ingredientName))
            .check(matches(isDisplayed()))
            .perform(typeText("Test (ml)"))
        onView(withId(R.id.ingredientAmount))
            .check(matches(isDisplayed()))
            .perform(typeText("100"))
        onView(withId(R.id.categoryChipMain)).perform(click())
        scenario.withFragment {
            setSelectedImageUri(null)
        }

        Thread.sleep(1500)
        onView(withId(R.id.recipeSaveButton)).perform(scrollTo(), click())

        onView(withId(R.id.emptyMediaSelectionView)).check(matches(isDisplayed()))
    }

    @Test
    fun createFragment_validation_succeeds_on_valid_inputs() {
        val scenario = launchFragmentInContainer<CreateFragment>(themeResId = R.style.Theme_CookstaApp)

        val mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        RetrofitClient.useMockServerUrl(mockWebServer.url("/api/"))

        onView(withId(R.id.recipeTitleInput)).perform(typeText("Test recipe title"))
        onView(withId(R.id.recipeDescriptionInput)).perform(typeText("Recipe description"))
        onView(withId(R.id.addIngredientButton)).perform(click())
        onView(withId(R.id.ingredientName))
            .check(matches(isDisplayed()))
            .perform(typeText("Test (ml)"))
        onView(withId(R.id.ingredientAmount))
            .check(matches(isDisplayed()))
            .perform(typeText("100"))
        onView(withId(R.id.categoryChipMain)).perform(click())
        scenario.withFragment {
            setSelectedImageUri(Uri.parse("content://someUri"))
        }

        Thread.sleep(1500)
        onView(withId(R.id.recipeSaveButton)).perform(scrollTo(), click())

        onView(withId(R.id.recipeTitleLayout)).check(matches(hasNoError()))
        onView(withId(R.id.recipeDescriptionLayout)).check(matches(hasNoError()))
        onView(withId(R.id.emptyCategorySelectionView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.emptyMediaSelectionView)).check(matches(not(isDisplayed())))
    }
}
