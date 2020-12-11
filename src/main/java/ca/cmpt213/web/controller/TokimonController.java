package ca.cmpt213.web.controller;

import ca.cmpt213.web.model.Tokimon;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

//TODO: check to see if we should return the tokimons object or not from gettokilist
//otherwise complete

@RestController
public class TokimonController {

    private final List<Tokimon> tokimons = new ArrayList<>();

    //command to get list of tokimon objects from .json file
    //curl -i -X GET localhost:8080/api/tokimon/all
    @GetMapping("/api/tokimon/all")
    public JSONArray getTokiList(HttpServletResponse response) {
        JSONParser jsonParser = new JSONParser();
        //Get .json file to read from
        try (FileReader reader = new FileReader("data/tokimons.json")){
            //Read file
            Object obj = jsonParser.parse(reader);
            JSONArray tokimonList = (JSONArray) obj;
            return tokimonList;
        }
        catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        response.setStatus(200);
        System.out.println("error");
        return null;
    }

    //command to get a specific toki ID
    //curl -i -X GET localhost:8080/api/tokimon/num
    @GetMapping("api/tokimon/{id}")
    public Tokimon getToki(@PathVariable int id, HttpServletResponse response) {
        for (int i = 0; i < tokimons.size(); i++) {
            if (tokimons.get(i).getPrivateId() == id) {
                response.setStatus(200);
                System.out.println("Tokimon found");
                return tokimons.get(i);
            }
        }
        System.out.println("Tokimon not found");
        invalidTokiId(response);
        return null;
    }

    //command to add a toki
    //curl -i -X POST -d "{\"name\":\"kenny\",\"weight\":155,\"height\":1.73,\"ability\":\"builtdiff\",\"strength\":100,\"colour\":\"red\"}" -H "Content-Type: application/json" localhost:8080/api/tokimon/add
    //curl -i -X POST -d "{\"name\":\"deku\",\"weight\":140,\"height\":1.70,\"ability\":\"One For All\",\"strength\":99,\"colour\":\"green\"}" -H "Content-Type: application/json" localhost:8080/api/tokimon/add
    //curl -i -X POST -d "{\"name\":\"goku\",\"weight\":200,\"height\":200,\"ability\":\"Super Saiyan\",\"strength\":9000,\"colour\":\"yellow\"}" -H "Content-Type: application/json" localhost:8080/api/tokimon/add
    @PostMapping("api/tokimon/add")
    public void addToki(@RequestBody Tokimon toki, HttpServletResponse response) {
        tokimons.add(toki);
        System.out.println("Tokimon created");
        print();
        writeJSONFile();
        response.setStatus(201);
    }

    //command to alter a toki
    //curl -i -X POST localhost:8080/api/tokimon/change/1?name=bryan^&weight=145^&height=1.70^&ability=anger^&strength=50^&color=green
    @PostMapping("api/tokimon/change/{id}")
    public void alterToki(@PathVariable int id, HttpServletResponse response,
                          @RequestParam(value="name", defaultValue = "DEFAULT_NAME") String name,
                          @RequestParam(value="weight", defaultValue = "0") double weight,
                          @RequestParam(value="height", defaultValue = "0") double height,
                          @RequestParam(value="ability", defaultValue = "DEFAULT_ABILITY") String ability,
                          @RequestParam(value="strength", defaultValue = "0") int strength,
                          @RequestParam(value="color", defaultValue = "DEFAULT_COLOR") String color){

        for (int i = 0; i < tokimons.size(); i++) {
            System.out.println("before: " +tokimons.get(i).toString());
            if (tokimons.get(i).getPrivateId() == id) {
                if (!name.equals("DEFAULT_NAME")) {
                    tokimons.get(i).setName(name);
                }
                if (weight != 0) {
                    tokimons.get(i).setWeight(weight);
                }
                if (height != 0) {
                    tokimons.get(i).setHeight(height);
                }
                if (!ability.equals("DEFAULT_ABILITY")) {
                    tokimons.get(i).setAbility(ability);
                }
                if (strength != 0) {
                    tokimons.get(i).setStrength(strength);
                }
                if (!color.equals("DEFAULT_COLOR")) {
                    tokimons.get(i).setColour(color);
                }
                System.out.println("after: " +tokimons.get(i).toString());
                writeJSONFile();
                response.setStatus(201);
                return;
            }
        }
        System.out.println("Tokimon not found");
        invalidTokiId(response);
    }

    //command to delete a toki
    //curl -i -X DELETE localhost:8080/api/tokimon/1
    @DeleteMapping("api/tokimon/{id}")
    public void deleteToki(@PathVariable int id, HttpServletResponse response) {
        for (int i = 0; i < tokimons.size(); i++) {
            if (tokimons.get(i).getPrivateId() == id) {
                System.out.println("Tokimon deleted");
                tokimons.remove(i);
                print();
                writeJSONFile();
                response.setStatus(204);
                return;
            }
        }
        System.out.println("Tokimon not found");
        invalidTokiId(response);
    }

    //grab tokimon.json and add it to list before server goes live
    @PostConstruct
    public void init() {
        readJSONFile();
    }

    private void invalidTokiId(HttpServletResponse response) {
        try {
            response.sendError(404, "Invalid Tokimon ID");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void print() {
        for (int i = 0; i < tokimons.size(); i++ ) {
            System.out.println(tokimons.get(i).getPrivateId());
            System.out.println(tokimons.get(i).toString());
        }
    }

    private void writeJSONFile() {
        JSONArray tokimonList = new JSONArray();
        for (int i =0; i< tokimons.size(); i++) {
            //Create new tokimon JSON object for each tokimon in list
            JSONObject tokimonDetails = new JSONObject();

            //Put tokimon's attributes into JSON object
            tokimonDetails.put("name", tokimons.get(i).getName());
            tokimonDetails.put("weight", tokimons.get(i).getWeight());
            tokimonDetails.put("height", tokimons.get(i).getHeight());
            tokimonDetails.put("ability", tokimons.get(i).getAbility());
            tokimonDetails.put("strength", tokimons.get(i).getStrength());
            tokimonDetails.put("color", tokimons.get(i).getColour());
            tokimonDetails.put("privateId", tokimons.get(i).getPrivateId());

            //Put tokimon JSON object into JSON array
            JSONObject tokimonObject = new JSONObject();
            tokimonObject.put("tokimon", tokimonDetails);
            tokimonList.add(tokimonObject);
        }

        //Write JSON file
        try (FileWriter file = new FileWriter("data/tokimons.json")) {
            file.write(tokimonList.toJSONString());
            file.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJSONFile() {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        //Get .json file to read from
        try (FileReader reader = new FileReader("data/tokimons.json")){
            //Read file
            Object obj = jsonParser.parse(reader);
            JSONArray tokimonList = (JSONArray) obj;
            System.out.println(tokimonList);
            //Iterate over Tokimon array
           tokimonList.forEach( toki -> parseTokimonObject( (JSONObject) toki ) );
        }
        catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }
    private void parseTokimonObject(JSONObject tokimon)
    {
        //Get tokimon object within list
        JSONObject tokimonObject = (JSONObject) tokimon.get("tokimon");

        //Parse tokimon's attributes from JSONObject
        String name = (String) tokimonObject.get("name");
        double weight = (double) tokimonObject.get("weight");
        double height = (double) tokimonObject.get("height");
        String ability = (String) tokimonObject.get("ability");
        long str = (long) tokimonObject.get("strength");
        int strength = (int) str;
        String color = (String) tokimonObject.get("color");
        long pid = (long) tokimonObject.get("privateId");
        int privateId = (int) pid;

        //Creating tokimon and adding it to list
        Tokimon newTokimon = new Tokimon(name,weight,height,ability,strength,color);
        newTokimon.setPrivateId(privateId);
        tokimons.add(newTokimon);
    }
}