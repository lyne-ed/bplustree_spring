package fr.miage.btree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@SpringBootApplication
public class BtreeApplication {

    int index = 0;
    // The tree key and value are both String, but you can change it to whatever you want, Generic types are used
    Btree<String, String> bplustree;

    public static void main(String[] args) {
        SpringApplication.run(BtreeApplication.class, args);
    }

    /**
     * This method is called on the application startup thanks to the @PostConstruct annotation
     * It is used to initialize the tree with some random data
     */
    @PostConstruct
    public void init() {
        bplustree = new Btree<String, String>();

        Faker faker = new Faker();
        IntStream.range(0, 13).forEach(
                i ->
                        // Used to test the tree building
//                        bplustree.insert(faker.funnyName().name(), faker.address().fullAddress())

                        // Used to test the Angular frontend
                        bplustree.insert(String.valueOf(i), String.valueOf(i))
        );
    }

    /**
     * This endpoint is used to render the tree in the browser, with a json format
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/")
    public String index() throws JsonProcessingException {
        return renderView(bplustree);
    }

    /**
     * This method is used to illustrate the "add" process
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/add")
    public String add() throws JsonProcessingException {

        Faker faker = new Faker();

        bplustree.insert(faker.funnyName().name(), faker.address().fullAddress());
        index++;

        return renderView(bplustree);
    }

    /**
     * This method is used to illustrate the "delete"
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/delete")
    public String delete() throws JsonProcessingException {
        Btree<Integer, Integer> bplustree = new Btree<>();
        index = 0;
        return renderView(bplustree);
    }


/**
     * This method is used to render the tree in the browser, with a json format
     * @param btree
     * @return
     * @throws JsonProcessingException
     */
    public String renderView(Btree btree) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        String result = mapper
                .writerWithView(Views.Public.class)
                .writeValueAsString(btree);

        return result;
    }

}
