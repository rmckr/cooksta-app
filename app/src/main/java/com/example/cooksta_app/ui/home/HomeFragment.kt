package com.example.cooksta_app.ui.home

import com.example.cooksta_app.model.Post
import com.example.cooksta_app.ui.shared.FeedFragment

class HomeFragment : FeedFragment() {
    override fun providePosts(): List<Post> {
        return listOf(
            Post("https://www.koch-mit.de/app/uploads/2020/01/jaegerpfanne-754x330.jpg", null),
            Post("https://example.com/image2.jpg", "https://files.vecteezy.com/system/resource/files/5273758/h81.mov?response-content-disposition=attachment%3Bfilename%3Dvecteezy_chef-cooking-with-fire-in-frying-pan-on-a-street-food-festival_5273758.mov&Expires=1737716047&Signature=p7q-XIC0C6zVU5HHpUOmuNMyspVPS-uL0dxECjf4GQwr~Sc3cNppjsfpfrEtmyajaOZz0ElqyjKqsyLKzzKGWyU78H3F6fOaEiMf-iFObIYyQIOlO5~ItV8GT4IZUvtlXekgUGPnnVWMNVh8dEck1k4XyxJXFjzspzQL5rgqlsKcH~Z2yIACX-mdQyv3Yy5fCBXH9T~68tCZYJvfX3w9IJJ3bcWxb6z9fpReWoZ5zfz7x~fEvlp9ez~1RDC91dEK9C~KU73lRGM60GsLpcwrlANCcJVA0yM~4c7T0EWr3CG15P7QAee1o~BIz8Y9v8YOA7pF~NCXFQuvV5fIMSC1Jw__&Key-Pair-Id=K3714PYOSHV3HB"),
            Post("https://www.simply-v.de/volumes/recipe/collections/_768x838_crop_center-center_none/rezepte-mittagessen-simply-v.jpg?v=1720165883", null)
        )
    }
}
