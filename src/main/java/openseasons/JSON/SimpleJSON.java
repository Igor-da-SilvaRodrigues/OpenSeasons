package openseasons.JSON;

import com.google.gson.JsonObject;
import net.fabricmc.loader.impl.lib.gson.JsonReader;
import net.fabricmc.loader.impl.lib.gson.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class SimpleJSON {
    private static final Logger LOGGER = LoggerFactory.getLogger("SimpleJSON");
    /**
     * Saves the JsonObject to a file in the specified path.
     * @param path The path to the desired file
     * @param json The json object
     * @throws IOException If the file does not exist but cannot be created
     * @throws SecurityException If a security does not permit the directory to be created
     */
    public static void saveTo(String path, JsonObject json) throws IOException, SecurityException{

        File file = new File(path);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            if(!dir.mkdir()){
                LOGGER.error("Failed to create nonexistent directory.");
                return;
            }
        }

        try(FileWriter writer = new FileWriter(file)){
            writer.write(json.toString());
        }
    }

    /**
     * Retrieves JsonObject from the specified path and returns it.
     * @param path The path to the file containing the desired json object
     * @return The desired Json object
     * @throws java.io.IOException if an unexpected value was read when trying to read a name.
     * @throws IllegalStateException if an unexpected value was read.
     * @throws NumberFormatException if an unexpected value was read when trying to read an int value.
     */
    public static JsonObject loadFrom(String path) throws java.io.IOException, IllegalStateException, NumberFormatException {
        JsonObject jsonObject = new JsonObject();
        try(FileReader reader = new FileReader(path)){
            jsonObject = load(reader);
        }

        return  jsonObject;
    }

    private static JsonObject load(Reader reader) throws java.io.IOException, IllegalStateException, NumberFormatException{
        JsonObject jsonObject = new JsonObject();

        String string = null;
        Integer integer = null;
        Boolean bool = null;
        String name = null;

        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            JsonToken token = jsonReader.peek();

            if (token == JsonToken.NAME){
                name = jsonReader.nextName();
            }else if (token == JsonToken.NUMBER){
                integer = jsonReader.nextInt();
                jsonObject.addProperty(name, integer);//no java, 'name' may not be null. But I will not waste my time
                // explaining to you why that is the case.

            }else if (token == JsonToken.STRING){
                string = jsonReader.nextString();
                jsonObject.addProperty(name, string);

            }else if (token == JsonToken.BOOLEAN){
                bool =  jsonReader.nextBoolean();
                jsonObject.addProperty(name, bool);

            }
        }
        jsonReader.endObject();
        return jsonObject;
    }
}