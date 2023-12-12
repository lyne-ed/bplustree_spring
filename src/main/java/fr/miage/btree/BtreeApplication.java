package fr.miage.btree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class BtreeApplication {
    Btree<Integer, Integer> st;
    int index = 0;

    public static void main(String[] args) {
        SpringApplication.run(BtreeApplication.class, args);
    }

    @GetMapping("/add")
    public String add() throws JsonProcessingException {
        if (st == null)
            st = new Btree<Integer, Integer>();

        st.insert(index, index);
        index++;

        return renderView(st);
    }

    @GetMapping("/delete")
    public String delete() throws JsonProcessingException {
        st = new Btree<Integer, Integer>();
        index = 0;
        return renderView(st);
    }

    public String renderView(Btree btree) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        String result = mapper
                .writerWithView(Views.Public.class)
                .writeValueAsString(btree);

        return result;
    }



//    @GetMapping("/")
//    public String index() throws JsonProcessingException {
//        st = new Btree<String, String>();
//
////        st.insert("www.cs.princeton.edu", "128.112.136.12");
////        st.insert("www.cs.princeton.edu", "128.112.136.11");
////        st.insert("www.princeton.edu",    "128.112.128.15");
////        st.insert("www.yale.edu",         "130.132.143.21");
////        st.insert("www.simpsons.com",     "209.052.165.60");
////        st.insert("www.apple.com",        "17.112.152.32");
////        st.insert("www.amazon.com",       "207.171.182.16");
////        st.insert("www.ebay.com",         "66.135.192.87");
////        st.insert("www.cnn.com",          "64.236.16.20");
////        st.insert("www.google.com",       "216.239.41.99");
////        st.insert("www.nytimes.com",      "199.239.136.200");
////        st.insert("www.microsoft.com",    "207.126.99.140");
////        st.insert("www.dell.com",         "143.166.224.230");
////        st.insert("www.slashdot.org",     "66.35.250.151");
////        st.insert("www.espn.com",         "199.181.135.201");
////        st.insert("www.weather.com",      "63.111.66.11");
////        st.insert("www.yahoo.com",        "216.109.118.65");
//
////        st.insert("1", "1");
////        st.insert("2", "2");
////        st.insert("3", "3");
////        st.insert("4", "4");
////        st.insert("5", "5");
////        st.insert("6", "6");
////        st.insert("7", "7");
////        st.insert("8", "8");
////        st.insert("9", "9");
////        st.insert("10", "10");
////        st.insert("11", "11");
////        st.insert("12", "12");
////        st.insert("13", "13");
////        st.insert("14", "14");
////        st.insert("15", "15");
////        st.insert("16", "16");
////        st.insert("17", "17");
////        st.insert("18", "18");
////        st.insert("19", "19");
//
//
//
//        System.out.println("cs.princeton.edu:  " + st.search("www.cs.princeton.edu"));
//        System.out.println("hardvardsucks.com: " + st.search("www.harvardsucks.com"));
//        System.out.println("simpsons.com:      " + st.search("www.simpsons.com"));
//        System.out.println("apple.com:         " + st.search("www.apple.com"));
//        System.out.println("ebay.com:          " + st.search("www.ebay.com"));
//        System.out.println("dell.com:          " + st.search("www.dell.com"));
//        System.out.println();
//
//        //System.out.println("size:    " + st.size());
//        //System.out.println("height:  " + st.height());
//        System.out.println(st);
//        System.out.println();
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
//
//        String result = mapper
//                .writerWithView(Views.Public.class)
//                .writeValueAsString(st);
//
//        return result;
//    }
}
