package net.viciont.cinematics.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utilidad para manejar archivos de configuración JSON
 * 
 * @author CrissyjuanxD
 */
public class ConfiguracionJSON {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private JsonObject objetoJson = new JsonObject();
    private final File archivo;
    
    public ConfiguracionJSON(String nombreArchivo, String ruta) throws IOException {
        this.archivo = new File(ruta + File.separator + nombreArchivo);
        
        if (!archivo.exists()) {
            archivo.getParentFile().mkdirs();
            escribirArchivo();
        } else {
            leerArchivo();
        }
    }
    
    public ConfiguracionJSON(String nombreArchivo) throws IOException {
        this(nombreArchivo, System.getProperty("user.dir") + File.separator + "plugins" + File.separator + "ViciontCinematics");
    }
    
    /**
     * Guarda los cambios en el archivo
     */
    public void guardar() throws IOException {
        escribirArchivo();
    }
    
    /**
     * Recarga el archivo desde el disco
     */
    public void recargar() throws IOException {
        leerArchivo();
    }
    
    /**
     * Escribe el objeto JSON al archivo
     */
    private void escribirArchivo() throws IOException {
        try (FileWriter writer = new FileWriter(archivo)) {
            gson.toJson(objetoJson, writer);
        }
    }
    
    /**
     * Lee el objeto JSON desde el archivo
     */
    private void leerArchivo() throws IOException {
        try (FileReader reader = new FileReader(archivo)) {
            JsonObject objeto = gson.fromJson(reader, JsonObject.class);
            if (objeto != null) {
                this.objetoJson = objeto;
            }
        }
    }
    
    /**
     * Obtiene una propiedad como String
     */
    public String obtenerString(String clave, String valorPorDefecto) {
        if (objetoJson.has(clave) && !objetoJson.get(clave).isJsonNull()) {
            return objetoJson.get(clave).getAsString();
        }
        return valorPorDefecto;
    }
    
    /**
     * Establece una propiedad String
     */
    public void establecerString(String clave, String valor) {
        objetoJson.addProperty(clave, valor);
    }
    
    /**
     * Obtiene una propiedad como int
     */
    public int obtenerInt(String clave, int valorPorDefecto) {
        if (objetoJson.has(clave) && !objetoJson.get(clave).isJsonNull()) {
            return objetoJson.get(clave).getAsInt();
        }
        return valorPorDefecto;
    }
    
    /**
     * Establece una propiedad int
     */
    public void establecerInt(String clave, int valor) {
        objetoJson.addProperty(clave, valor);
    }
    
    /**
     * Obtiene una propiedad como boolean
     */
    public boolean obtenerBoolean(String clave, boolean valorPorDefecto) {
        if (objetoJson.has(clave) && !objetoJson.get(clave).isJsonNull()) {
            return objetoJson.get(clave).getAsBoolean();
        }
        return valorPorDefecto;
    }
    
    /**
     * Establece una propiedad boolean
     */
    public void establecerBoolean(String clave, boolean valor) {
        objetoJson.addProperty(clave, valor);
    }
    
    /**
     * Verifica si existe una clave
     */
    public boolean tienePropiedad(String clave) {
        return objetoJson.has(clave);
    }
    
    /**
     * Remueve una propiedad
     */
    public void removerPropiedad(String clave) {
        objetoJson.remove(clave);
    }
    
    // Getters y Setters
    public JsonObject getObjetoJson() {
        return objetoJson;
    }
    
    public void setObjetoJson(JsonObject objetoJson) {
        this.objetoJson = objetoJson;
    }
    
    public File getArchivo() {
        return archivo;
    }
}