package de.htwg.in.schneider.casellese.backend_casellese.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.htwg.in.schneider.casellese.backend_casellese.model.Product;
import de.htwg.in.schneider.casellese.backend_casellese.model.Category;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @GetMapping
    public List<Product> getProducts() {
        Product kaese = new Product();
        kaese.setId(1);
        kaese.setTitle("Caciocavallo");
        kaese.setDescription("Ein halbfester bis harter Käse aus Kuhmilch, der in Süditalien hergestellt wird.");
        kaese.setCategory(Category.KAESE);
        kaese.setPrice(12.99);
        kaese.setImageUrl("https://nucccio.github.io/casellese-images/caciocavallo.webp");
        kaese.setImageUrl_Details("https://nucccio.github.io/casellese-images/caciocavallo-rezepte.webp");

        Product salami = new Product();
        salami.setId(2);
        salami.setTitle("Salsiccia");
        salami.setDescription("Eine italienische Rohwurst, die aus Schweinefleisch und Gewürzen hergestellt wird.");
        salami.setCategory(Category.SALAMI);
        salami.setPrice(9.99);
        salami.setImageUrl("https://nucccio.github.io/casellese-images/salsiccia.webp");
        salami.setImageUrl_Details("https://nucccio.github.io/casellese-images/salsiccia-rezepte.webp");

        Product brot = new Product();
        brot.setId(3);
        brot.setTitle("Focaccia");
        brot.setDescription("Ein flaches italienisches Brot, das mit Olivenöl, Salz und Kräutern belegt ist.");
        brot.setCategory(Category.BROT);
        brot.setPrice(4.99);
        brot.setImageUrl("https://nucccio.github.io/casellese-images/brot.webp");
        brot.setImageUrl_Details("https://nucccio.github.io/casellese-images/brot-rezepte.webp");

        return Arrays.asList(kaese, salami, brot);
    }
    
}
