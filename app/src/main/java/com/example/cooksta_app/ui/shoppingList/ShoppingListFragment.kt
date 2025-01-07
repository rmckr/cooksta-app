package com.example.cooksta_app.ui.shoppingList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentIngredientsBinding
import com.example.cooksta_app.databinding.FragmentRecipeBinding
import com.example.cooksta_app.databinding.FragmentShoppingListEmptyBinding
import com.example.cooksta_app.databinding.FragmentShoppingListFullBinding
import com.google.android.material.tabs.TabLayoutMediator

class ShoppingListFragment : Fragment() {

    private var _emptyBinding: FragmentShoppingListEmptyBinding? = null
    private var _fullBinding: FragmentShoppingListFullBinding? = null

    private val emptyBinding get() = _emptyBinding!!
    private val fullBinding get() = _fullBinding!!

    private val recipes = mutableListOf<Recipe>()
    private val ingredients = mutableListOf<Ingredient>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fillShoppingListWithIngredientsAndRecipes() // Füllt die Einkaufsliste mit Rezepten und Zutaten

        // Überprüfe, ob entweder Rezepte oder Zutaten vorhanden sind
        return if (recipes.isEmpty() && ingredients.isEmpty()) {
            // Leeres Layout verwenden, wenn keine Rezepte oder Zutaten vorhanden sind
            _emptyBinding = FragmentShoppingListEmptyBinding.inflate(inflater, container, false)
            emptyBinding.root
        } else {
            // Volles Layout verwenden, wenn Rezepte oder Zutaten vorhanden sind
            _fullBinding = FragmentShoppingListFullBinding.inflate(inflater, container, false)
            setupShoppingListUI() // UI für gefüllte Liste initialisieren
            fullBinding.root
        }
    }

    private fun fillShoppingListWithIngredientsAndRecipes() {
        // Beispielweise: Füllen der Einkaufslisten mit Rezepten und Zutaten
        recipes.addAll(
            listOf(
                Recipe("Spaghetti Bolognese"),
                Recipe("Pizza Margherita"),
                Recipe("Caesar Salad")
            )
        )

        ingredients.addAll(
            listOf(
                Ingredient("Tomaten", "3 Stück"),
                Ingredient("Käse", "200 g"),
                Ingredient("Olivenöl", "50 ml")
            )
        )
    }

    private fun setupShoppingListUI() {
        // Adapter für den ViewPager2
        val fragmentList = mutableListOf<Fragment>()

        // Hinzufügen der Rezepte und Zutaten, wenn Daten vorhanden sind
        if (recipes.isNotEmpty()) {
            fragmentList.add(RecipeFragment()) // Fragment für Rezepte
        }
        if (ingredients.isNotEmpty()) {
            fragmentList.add(IngredientsFragment()) // Fragment für Zutaten
        }

        // Wenn beide Fragmente leer sind, gehe zurück und zeige leeres Layout
        if (fragmentList.isEmpty()) {
            return
        }

        val adapter = ShoppingListPagerAdapter(this, fragmentList)
        fullBinding.viewPager.adapter = adapter

        // TabLayout mit ViewPager2 verknüpfen
        TabLayoutMediator(fullBinding.tabLayout2, fullBinding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.recipe)      // Tab-Titel für Rezepte
                1 -> getString(R.string.ingredients) // Tab-Titel für Zutaten
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _emptyBinding = null
        _fullBinding = null
    }
}

class ShoppingListPagerAdapter(
    fragment: Fragment,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

data class Recipe(
    val name: String
)

class RecipesAdapter(private val recipes: List<Recipe>) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    // ViewHolder für das Rezept
    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
    }

    // Erstelle ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    // Binde Daten an ViewHolder
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeName.text = recipe.name
    }

    // Rückgabewert für die Anzahl der Rezepte (Anzahl der Elemente in der Liste)
    override fun getItemCount(): Int = recipes.size
}

class RecipeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding verwenden (oder findViewById, falls kein ViewBinding)
        val binding = FragmentRecipeBinding.inflate(inflater, container, false)

        // Erstelle eine Liste von Test-Rezepten
        val recipesList = listOf(
            Recipe("Spaghetti Bolognese"),
            Recipe("Pizza Margherita"),
            Recipe("Caesar Salad")
        )

        // Setze den Adapter für die RecyclerView
        val adapter = RecipesAdapter(recipesList)
        binding.recipeRecyclerView.adapter = adapter

        return binding.root
    }
}


class IngredientsAdapter(private val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    // ViewHolder für die Zutat
    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        val ingredientAmount: TextView = itemView.findViewById(R.id.ingredientAmount)
    }

    // Erstelle ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    // Binde Daten an ViewHolder
    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.ingredientName.text = ingredient.name
        holder.ingredientAmount.text = ingredient.amount
    }

    // Rückgabewert für Anzahl der Elemente
    override fun getItemCount(): Int = ingredients.size
}

data class Ingredient(
    val name: String,
    val amount: String
)

class IngredientsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentIngredientsBinding.inflate(inflater, container, false)

        // Erstelle eine Liste von Test-Zutaten
        val ingredientsList = listOf(
            Ingredient("Tomaten", "3 Stück"),
            Ingredient("Käse", "200 g"),
            Ingredient("Olivenöl", "50 ml")
        )

        // Setze den Adapter
        val adapter = IngredientsAdapter(ingredientsList)
        binding.ingredientRecyclerView.adapter = adapter
        return binding.root
    }
}