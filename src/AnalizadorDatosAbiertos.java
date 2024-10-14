import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.w3c.dom.Document;
import com.google.gson.JsonArray;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AnalizadorDatosAbiertos {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce la ruta del archivo a analizar:");

        String nombreArchivo = scanner.nextLine();
        String rutaArchivo = "data/" + nombreArchivo;

        if (rutaArchivo.endsWith(".csv")) {
            List<String[]> datosCSV = parsearCSV(rutaArchivo);
            mostrarResumenCSV(datosCSV);

        } else if (rutaArchivo.endsWith(".json")) {
            Object datosJSON = parsearJSON(rutaArchivo);
            if (datosJSON instanceof JsonObject) {
                mostrarResumenJSON((JsonObject) datosJSON);
            } else if (datosJSON instanceof JsonArray) {
                mostrarResumenJSON((JsonArray) datosJSON);
            }

        } else if (rutaArchivo.endsWith(".xml")) {
            Document datosXML = parsearXML(rutaArchivo);
            mostrarResumenXML(datosXML);

        } else {
            System.out.println("Formato de archivo no soportado.");
        }
    }

    public static List<String[]> parsearCSV(String rutaArchivo) {
        List<String[]> registros = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new
                FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(",");
                registros.add(valores);
            }

        } catch (Exception e) {
            System.out.println("Error al leer el archivo CSV: " +
                    e.getMessage());
        }
        return registros;
    }

    public static void mostrarResumenCSV(List<String[]> datos) {
        if (datos.isEmpty()) {
            System.out.println("No se encontraron datos.");
            return; }
        System.out.println("Resumen del archivo CSV:");
        System.out.println("Número total de filas: " + datos.size());
        System.out.println("Número de columnas: " + datos.get(0).length);
        System.out.println("\nPrimeros 5 registros:");
        for (int i = 0; i < Math.min(5, datos.size()); i++) {
            System.out.println(String.join(" | ", datos.get(i)));

        }
    }

    public static Object parsearJSON(String rutaArchivo) {
        try (FileReader reader = new FileReader(rutaArchivo)) {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);

            // Comprobamos si el elemento JSON es un objeto o un array
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();  // Retorna JsonObject
            } else if (jsonElement.isJsonArray()) {
                return jsonElement.getAsJsonArray();  // Retorna JsonArray
            } else {
                System.out.println("El archivo JSON no contiene un objeto ni un array en el nivel superior.");
            }
        } catch (Exception e) {
            System.out.println("Error al leer el archivo JSON: " + e.getMessage());
        }
        return null;
    }

    public static void mostrarResumenJSON(Object datos) {
        if (datos == null) {
            System.out.println("No se encontraron datos en el archivo JSON.");
            return;
        }

        if (datos instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) datos;
            System.out.println("Resumen del archivo JSON:");
            System.out.println("Número total de elementos: " + jsonObject.size());
            System.out.println("\nContenido de los primeros 5 elementos:");

            int contador = 0;
            for (String clave : jsonObject.keySet()) {
                System.out.println(clave + " : " + jsonObject.get(clave));
                contador++;
                if (contador >= 5) break;
            }
        } else if (datos instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) datos;
            System.out.println("Resumen del archivo JSON Array:");
            System.out.println("Número total de elementos en el array: " + jsonArray.size());
            System.out.println("\nPrimeros 5 elementos:");

            for (int i = 0; i < Math.min(5, jsonArray.size()); i++) {
                System.out.println(jsonArray.get(i));
            }
        } else {
            System.out.println("Tipo de datos no reconocido.");
        }
    }

    public static Document parsearXML(String rutaArchivo) {
        Document doc = null;
        try {
            String contenidoXML = new String(Files.readAllBytes(Paths.get(rutaArchivo)), StandardCharsets.UTF_8);

            // reemplazar la entidad "&plusmn;" con el símbolo "±" o eliminarla si no es necesaria
            contenidoXML = contenidoXML.replace("&plusmn;", "±");

            // escribir el contenido temporalmente a un archivo
            Files.write(Paths.get("temp.xml"), contenidoXML.getBytes(StandardCharsets.UTF_8));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse("temp.xml");
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            System.out.println("Error al leer el archivo XML: " + e.getMessage());
        }
        return doc;
    }

    public static void mostrarResumenXML(Document datos) {
        if (datos == null) {
            System.out.println("No se encontraron datos en el archivo XML.");
            return;
        }

        System.out.println("Resumen del archivo XML:");
        // elemento raíz del documento
        String nodoRaiz = datos.getDocumentElement().getNodeName();
        System.out.println("Nodo raíz: " + nodoRaiz);

        System.out.println("\nElementos hijos del nodo raíz:");

        var hijos = datos.getDocumentElement().getChildNodes();
        int contador = 0;
        for (int i = 0; i < hijos.getLength(); i++) {
            if (hijos.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                //nombre de los nodos
                var hijosNodoNombre = hijos.item(i).getNodeName();
                System.out.println("Elemento: " + hijosNodoNombre);

                // contenido del nodo: Atributos
                // verifica si el nodo es un Row y tiene atributos
                var subHijos = hijos.item(i).getChildNodes();
                for (int j = 0; j < subHijos.getLength(); j++) {
                    if (subHijos.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
                            subHijos.item(j).getNodeName().equals("Row")) {

                        System.out.println(" - Atributos de " + subHijos.item(j).getNodeName() + ":");
                        var atributos = subHijos.item(j).getAttributes();
                        for (int k = 0; k < atributos.getLength(); k++) {
                            var atributo = atributos.item(k);
                            System.out.println("    " + atributo.getNodeName() + " : " + atributo.getNodeValue());
                        }
                    }
                }
                contador++;
                if (contador >= 5) break;
            }
        }

        if (contador == 0) {
            System.out.println("El nodo raíz no tiene elementos hijos.");
        }
    }

}