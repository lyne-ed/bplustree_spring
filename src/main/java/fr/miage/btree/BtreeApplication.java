package fr.miage.btree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
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
		bplustree = new Btree<>();
		
		Faker faker = new Faker();
		IntStream.range(0, 13).forEach(
				i ->
						// Used to test the tree building
						//                        bplustree.insert(faker.funnyName().name(), faker.address().fullAddress())
						
						// Used to test the Angular frontend (simpler data)
						bplustree.insert(String.valueOf(i), String.valueOf(i))
		);
	}
	
	/**
	 * This endpoint is used to render the tree in the browser, with a json format
	 *
	 * @return
	 * @throws JsonProcessingException
	 */
	@GetMapping("/")
	public String index() throws JsonProcessingException {
		return renderView(bplustree);
	}
	
	/**
	 * This method is used to illustrate the "add" process
	 * /!\ take care to change the data type if you change the tree key and value type
	 *
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
	 *
	 * @return
	 * @throws JsonProcessingException
	 */
	@GetMapping("/delete")
	public String delete() throws JsonProcessingException {
		bplustree = new Btree<>();
		index = 0;
		return renderView(bplustree);
	}
	
	/**
	 * This method is used to render the tree in the browser, with a json format
	 *
	 * @param btree
	 * @return
	 * @throws JsonProcessingException
	 */
	public String renderView(Btree btree) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		
		return mapper
				       .writerWithView(Views.Public.class)
				       .writeValueAsString(btree);
	}
	
	/**
	 * This method is used to illustrate the "search" process
	 * @param filePath Path to the entries (CSV file)
	 * @return String containing the search result
	 * @throws Exception
	 */
	@GetMapping("/benchmark")
	public String benchmark(String filePath) throws Exception {
		Btree<String, String> btree = loadCSV(filePath);
		List<String> lines = Files.readAllLines(Paths.get(filePath));
		
		Random random = new Random();
		List<String> testKeys = random.ints(100, 0, lines.size())
				                        .mapToObj(lines::get)
				                        .map(line -> line.split(",")[0])
				                        .collect(Collectors.toList());
		
		long btreeTotalTime = 0;
		long btreeMinTime = Long.MAX_VALUE;
		long btreeMaxTime = Long.MIN_VALUE;
		
		for (String key : testKeys) {
			long startTime = System.nanoTime();
			btree.search(key);
			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			
			btreeTotalTime += duration;
			btreeMinTime = Math.min(btreeMinTime, duration);
			btreeMaxTime = Math.max(btreeMaxTime, duration);
		}
		
		long btreeAvgTime = btreeTotalTime / testKeys.size();
		
		long fileTotalTime = 0;
		long fileMinTime = Long.MAX_VALUE;
		long fileMaxTime = Long.MIN_VALUE;
		
		for (String key : testKeys) {
			long startTime = System.nanoTime();
			sequentialSearchInFile(key, filePath);
			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			
			fileTotalTime += duration;
			fileMinTime = Math.min(fileMinTime, duration);
			fileMaxTime = Math.max(fileMaxTime , duration);
		}
		
		long fileAvgTime = fileTotalTime / testKeys.size();
		
		String result = "B-tree Search Performance:<br>" +
				"Average Time: " + btreeAvgTime + " ns<br>" +
				"Minimum Time: " + btreeMinTime + " ns<br>" +
				"Maximum Time: " + btreeMaxTime + " ns<br>" +
				"<br>" +
				"Sequential File Search Performance:<br>" +
				"Average Time: " + fileAvgTime + " ns<br>" +
				"Minimum Time: " + fileMinTime + " ns<br>" +
				"Maximum Time: " + fileMaxTime + " ns";
		
		return result;
	}
	
	private static void sequentialSearchInFile(String key, String filePath) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(filePath));
		for (String line : lines)
			if (line.startsWith(key + ","))
				break;
	}
	
	@GetMapping("/save")
	public String saveTree(@RequestParam String filePath) {
		if (!filePath.endsWith(".json")) {
			return "Error: File path must end with .json";
		}
		
		try {
			String json = renderView(bplustree);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
				writer.write("[" + json + "]");
			}
			return "Tree saved successfully to " + filePath;
		} catch (IOException e) {
			e.printStackTrace();
			return "Failed to save tree: " + e.getMessage();
		}
	}
	
	/**
	 * Endpoint to trigger data import from a file.
	 *
	 * @param filePath Path to the file (supports only JSON and CSV files for now)
	 * @return String indicating the status of the import.
	 */
	@GetMapping("/import")
	public String importData(@RequestParam String filePath) {
		try {
			if(filePath.contains(".json"))
				importDataFromJSONFile(filePath);
			else if(filePath.contains(".csv"))
				importDataFromCSVFile(filePath);
			else
				return "Failed to import data: Invalid file format";
			return renderView(bplustree);
		}
		catch(Exception e) {
			e.printStackTrace();
			return "Failed to import data: " + e.getMessage();
		}
	}
	
	private void importDataFromJSONFile(String filePath) throws IOException {
		bplustree = loadJson(filePath);
	}
	
	private static Btree<String, String> loadJson(String filePath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			StringBuilder json = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				json.append(line);
			
			// Idk why but as the project uses it in the renderView method, I will just do the same
			// Btw this method is deprecated
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			
			return mapper.readValue(json.toString(), new TypeReference<Btree<String, String>>() {});
		}
	}
	
	private void importDataFromCSVFile(String filePath) throws IOException {
		bplustree = loadCSV(filePath);
	}
	
	private Btree<String, String> loadCSV(String filePath) throws IOException {
		Btree<String, String> btree = new Btree<>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if(parts.length == 2) {
					String key = parts[0].trim();
					String value = parts[1].trim();
					btree.insert(key, value);
				}
				else {
					System.err.println("Invalid line format: " + line);
				}
			}
		}
		
		return btree;
	}
}