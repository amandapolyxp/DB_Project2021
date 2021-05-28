/** **
 * =============================================
 * ============== Bases de Dados ===============
 * ============== LEI  2020/2021 ===============
 * =============================================
 * =================== Demo ====================
 * =============================================
 * =============================================
 * === Department of Informatics Engineering ===
 * =========== University of Coimbra ===========
 * =============================================
 * <p>
 *
 * Authors: 
 *   Nuno Antunes <nmsa@dei.uc.pt>
 *   BD 2021 Team - https://dei.uc.pt/lei/
 */
package pt.uc.dei.bd2021;

import java.sql.*;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.tomcat.util.json.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BDDemo{

    private static final Logger logger = LoggerFactory.getLogger(BDDemo.class);
    public ArrayList<String> tokens = new ArrayList();

    /**
     * GENERATE TOKEN
     *
     * Este metodo devolve uma string alfanumerica
     * que ira ser utilizada com token de validação
     * de autenticacao.
     *
     * @return
     */
    public String generateNemToken(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }

    /**
     * GENERATE EAN
     *
     *
     * @return
     */
    public Long generateEAN(){

        int leftLimit = 48; // numeral '0'
        int rightLimit = 57; // letter 'z'
        int targetStringLength = 13;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return Long.parseLong(generatedString);
    }

    @GetMapping("/")
    public String landing() {
        return "Hello World!  <br/>\n"
                + "<br/>\n"
                + "Check the sources for instructions on how to use the endpoints!<br/>\n"
                + "<br/>\n"
                + "BD 2021 Team<br/>\n"
                + "<br/>";
    }

    /**
     * Demo GET
     *
     *
     * Obtain all departments, in JSON format
     *
     * To use it, access: <br>
     * http://localhost:8080/departments/
     *
     *
     * @return
     */
    @GetMapping(value = "/departments/", produces = "application/json")
    @ResponseBody
    public List<Map<String, Object>> getAllDepartments() {
        logger.info("###              DEMO: GET /departments              ###");
        Connection conn = RestServiceApplication.getConnection();
        List<Map<String, Object>> payload = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT ndep, nome, local FROM dep");
            logger.debug("---- departments  ----");
            while (rows.next()) {
                Map<String, Object> content = new HashMap<>();
                logger.debug("'ndep': {}, 'nome': {}, 'localidade': {}",
                        rows.getInt("ndep"), rows.getString("nome"), rows.getString("local")
                );
                content.put("ndep", rows.getInt("ndep"));
                content.put("nome", rows.getString("nome"));
                content.put("localidade", rows.getString("local"));
                payload.add(content);
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
        return payload;
    }

    /**
     * Demo GET
     *
     *
     * Obtain department with {@code ndep}
     *
     * To use it, access: <br>
     * http://localhost:8080/departments/
     *
     *
     * @param ndep id of the department to be selected
     * @return data of the department
     */
    @GetMapping(value = "/departments/{ndep}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getDepartment(@PathVariable("ndep") int ndep) {
        logger.info("###              DEMO: GET /departments              ###");
        Connection conn = RestServiceApplication.getConnection();

        Map<String, Object> content = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT ndep, nome, local FROM dep WHERE ndep = ?")) {
            ps.setInt(1, ndep);
            ResultSet rows = ps.executeQuery();
            logger.debug("---- selected department  ----");
            if (rows.next()) {
                logger.debug("'ndep': {}, 'nome': {}, 'localidade': {}", rows.getInt("ndep"), rows.getString("nome"), rows.getString("local"));
                content.put("ndep", rows.getInt("ndep"));
                content.put("nome", rows.getString("nome"));
                content.put("localidade", rows.getString("local"));
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
        return content; // returns empty if error or none selected
    }

    /**
     * Demo POST
     *
     *
     * Add a new department in a JSON payload
     *
     * To use it, you need to use postman or curl:
     *
     * {@code curl -X POST http://localhost:8080/departments/ -H "Content-Type: application/json" -d
     * '{"localidade": "Polo II", "ndep": 69, "nome": "Seguranca"}'}
     *
     *
     */
    @PostMapping(value = "/departments/", consumes = "application/json")
    @ResponseBody
    public String createDepartment(@RequestBody Map<String, Object> payload) {

        logger.info("###              DEMO: POST /departments              ###");
        Connection conn = RestServiceApplication.getConnection();

        logger.debug("---- new department  ----");
        logger.debug("payload: {}", payload);

        Map<String, Object> content = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(""
                + "INSERT INTO dep (ndep, nome, local) "
                + "         VALUES (  ? ,   ? ,    ? )")) {
            ps.setInt(1, (int) payload.get("ndep"));
            ps.setString(2, (String) payload.get("nome"));
            ps.setString(3, (String) payload.get("localidade"));
            int affectedRows = ps.executeUpdate();
            conn.commit();

            if (affectedRows == 1) {
                return "Inserted!";
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return "Failed";
    }

    /**
     * Demo PUT
     *
     *
     * Update a department based on the a JSON payload
     *
     * o use it, you need to use postman or curl:
     *
     * {@code curl -X PUT http://localhost:8080/departments/ -H "Content-Type: application/json" -d '{"ndep": 69, "localidade": "Porto"}'}
     *
     */
    @PutMapping(value = "/departments/", consumes = "application/json")
    @ResponseBody
    public String updateDepartment(@RequestBody Map<String, Object> payload) {
        Token token = new Token();
        logger.info("###              DEMO: PUT /departments               ###");

        if (!payload.containsKey("ndep") || !payload.containsKey("localidade")) {
            logger.warn("ndep and localidade are required to update");
            return "ndep and localidade are required to update";
        }

        logger.info("---- update department  ----");
        logger.debug("content: {}", payload);
        Connection conn = RestServiceApplication.getConnection();
        if (conn == null) {
            return "DB Problem!";
        }

        Map<String, Object> content = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(""
                + "UPDATE dep"
                + "   SET local = ? "
                + " WHERE ndep = ?")) {

            ps.setString(1, (String) payload.get("localidade"));
            ps.setInt(2, (int) payload.get("ndep"));

            int affectedRows = ps.executeUpdate();
            conn.commit();
            return "Updated: " + affectedRows;
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return "Failed";
    }

    /**
     * POST para criar user
     * recebe username e password
     *
     * @param payload
     * @return
     */
    @PostMapping(value = "/dbproj/user", consumes = "application/json")
    @ResponseBody
    public String registerUser(@RequestBody Map<String, Object> payload){

        Connection conn = RestServiceApplication.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(""
                + "INSERT INTO utilizador (username, password, email) "
                + "         VALUES (  ? ,   ? ,   ? )")) {
            ps.setString(1, (String) payload.get("username"));
            ps.setString(2, (String) payload.get("password"));
            ps.setString(3, (String) payload.get("email"));
            int affectedRows = ps.executeUpdate();
            conn.commit();

            if (affectedRows == 1) {
                return "User Created!";
            }
        } catch (SQLException ex) {
            logger.error("Creating User - Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Creating User - Couldn't rollback", ex);
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Creating User - Error in DB - Final", ex);
            }
        }
        return "Failed";
    }

    /**
     * PUT autenticacao
     *
     *
     */
    @PutMapping(value = "/dbproj/user", consumes = "application/json")
    @ResponseBody
    public String authUser(@RequestBody Map<String, Object> payload) {

        Connection conn = RestServiceApplication.getConnection();

        if (!payload.containsKey("username") && !payload.containsKey("password")) {
            logger.warn("Preciso username e pass para login");
            return "Username/Password missing";
        }

        try (PreparedStatement stmt = conn.prepareStatement("SELECT username, password"
                                                                + " FROM utilizador"
                                                                + " WHERE  username = ? and password = ? ")) {
            stmt.setString(1, (String) payload.get("username"));
            stmt.setString(2, (String) payload.get("password"));
            ResultSet rows = stmt.executeQuery();
            if(rows.next()){
                String token = generateNemToken();
                tokens.add(token);
                return "authToken: " + token;
            }else{
                return "erro:";
            }

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
        return "erro:";
    }


    /**
     * POST CRIAR LEILAO
     *
     *
     */
    @PostMapping(value = "/dbproj/leilao", consumes = "application/json")
    @ResponseBody
    public String createAuction(@RequestBody Map<String, Object> payload) {
        Connection conn = RestServiceApplication.getConnection();

        if(payload.get("authToken") == null || tokens.contains(payload.get("authToken")) == false){
            return "authError";
        }
        Map<String, Object> content = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(""
                + "INSERT INTO vendedor_artigo (artigo_nome, artigo_precoinicial, artigo_datalimite, artigo_ean, utilizador_username)"
                + "         VALUES ( ? , ? , ? , ?, ?)")) {
            ps.setString(1, (String) payload.get("nome_artigo"));
            ps.setDouble(2, (Double) payload.get("preco_inicial"));

            int year = (int)payload.get("year");
            int month = (int)payload.get("month");
            int day = (int)payload.get("day");
            int hour = (int)payload.get("hour");
            int min = (int)payload.get("minute");
            LocalDateTime date = LocalDateTime.of(year, month, day, hour, min);
            Timestamp time = Timestamp.valueOf(date);

            ps.setTimestamp(3, time);
            ps.setLong(4, generateEAN());
            ps.setString(5, (String) payload.get("username"));
            int affectedRows = ps.executeUpdate();
            conn.commit();

            if (affectedRows == 1) {
                return "Inserted!";
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return "Failed";
    }

    /**
     * GET LISTAR TODOS OS LEILOES
     *
     *
     * @return
     */
    @GetMapping(value = "/dbproj/leiloes", produces = "application/json")
    @ResponseBody
    public List<Map<String, Object>> getLeiloes() {
        logger.info("###              GET / Leiloes              ###");
        Connection conn = RestServiceApplication.getConnection();
        List<Map<String, Object>> payload = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT artigo_ean, artigo_nome FROM vendedor_artigo");
            logger.debug("---- vendedor_artigo  ----");
            while (rows.next()) {
                Map<String, Object> content = new HashMap<>();
                logger.debug("'leilaoID': {}, 'descricao': {}",
                        rows.getLong("artigo_ean"), rows.getString("artigo_nome")
                );
                content.put("leilaoID", rows.getLong("artigo_ean"));
                content.put("descricao", rows.getString("artigo_nome"));
                payload.add(content);
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
        return payload;
    }

    /**
     * GET PESQUISAR LEILOES EXISTENTES
     *  por ean ou por descricao (nome do artigo)
     *
     *
     * @return
     */
    @GetMapping(value = "/dbproj/leiloes/{keyword}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getDepartment(@PathVariable("keyword") String keyword) {
        logger.info("###              GET /leiloes              ###");
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> content = new HashMap<>();
        long artigo_ean;
        if (keyword.length() == 13 &&  Long.parseLong(keyword) > 0) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT artigo_ean, artigo_nome FROM vendedor_artigo WHERE artigo_ean = ?")) {
                ps.setLong(1, Long.parseLong(keyword));
                ResultSet rows = ps.executeQuery();
                logger.debug("---- selected ean  ----");
                if (rows.next()) {
                    logger.debug("'leilaoID': {}, 'descricao': {}", rows.getLong("artigo_ean"), rows.getString("artigo_nome"));
                    content.put("leilaoID", rows.getLong("artigo_ean"));
                    content.put("descricao", rows.getString("artigo_nome"));
                }
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        } else{
            try (PreparedStatement ps = conn.prepareStatement("SELECT artigo_ean, artigo_nome FROM vendedor_artigo WHERE artigo_nome = ?")) {
                ps.setString(1, keyword);
                ResultSet rows = ps.executeQuery();
                logger.debug("---- selected descricao  ----");
                if (rows.next()) {
                    logger.debug("'leilaoID': {}, 'descricao': {}", rows.getLong("artigo_ean"), rows.getString("artigo_nome"));
                    content.put("leilaoID", rows.getLong("artigo_ean"));
                    content.put("descricao", rows.getString("artigo_nome"));
                }
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return content; // returns empty if error or none selected
    }

    /**
     * GET CONSULTAR DETALHES DE UM LEILAO
     *  por ean
     *
     * @return
     */
    @GetMapping(value = "/dbproj/leilao/{leilaoId}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getDepartment(@PathVariable("leilaoId") long artigo_ean) {
        logger.info("###              GET /leiloes              ###");
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> content = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM vendedor_artigo, mural WHERE vendedor_artigo.artigo_ean = ?")) {
            ps.setLong(1, artigo_ean);
            ResultSet rows = ps.executeQuery();
            logger.debug("---- selected ean  ----");
            if (rows.next()) {
                logger.debug("'leilaoID': {}, 'descricao': {}, 'preco inicial': {}, 'preco final': {}, 'data limite': {}, 'vendedor':{}",
                         rows.getLong("artigo_ean"), rows.getString("artigo_nome"), rows.getDouble("artigo_precoinicial"),
                         rows.getDouble("artigo_precoatual"), rows.getTimestamp("artigo_dataLimite").toString(),
                         rows.getString("utilizador_username")
                        );
                content.put("leilaoID", rows.getLong("vendedor_artigo.artigo_ean"));
                content.put("descricao", rows.getString("vendedor_artigo.artigo_nome"));
                content.put("preco inicial", rows.getDouble("vendedor_artigo.artigo_precoinicial"));
                content.put("preco atual", rows.getDouble("vendedor_artigo.artigo_precoatual"));
                content.put("data limite", rows.getTimestamp("vendedor_artigo.artigo_datalimite"));
                content.put("vendedor", rows.getString("vendedor_artigo.utilizador_username"));
                content.put("comentarios", rows.getString("mural.comentario"));
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
        return content;
    }
}