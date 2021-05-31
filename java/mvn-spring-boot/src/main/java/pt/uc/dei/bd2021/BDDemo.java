/**
 * *
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
 * <p>
 * Authors:
 * Nuno Antunes <nmsa@dei.uc.pt>
 * BD 2021 Team - https://dei.uc.pt/lei/
 */
package pt.uc.dei.bd2021;

import java.sql.*;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.tomcat.util.json.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

class User{
    private String username;
    private String token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

@RestController
public class BDDemo {

    private static final Logger logger = LoggerFactory.getLogger(BDDemo.class);
    public ArrayList<User> tokens = new ArrayList();

    public String findToken(String token){
        for(User user : tokens){
            if(user.getToken().equals(token)){
                return user.getUsername();
            }
        }
        return null;
    }

    /**
     * GENERATE TOKEN
     *
     * Este metodo devolve uma string alfanumerica
     * que ira ser utilizada com token de validação
     * de autenticacao.
     *
     * @return
     */
    public String generateNemToken() {
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
    public Long generateEAN() {

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


//:___________________________________________________________________________________________________
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

    //::::_____________________________________________________________________________________


    /**
     * POST para criar user
     * recebe username e password
     *
     * @param payload
     * @return
     */
    @PostMapping(value = "/dbproj/user", consumes = "application/json")
    @ResponseBody
    public String registerUser(@RequestBody Map<String, Object> payload) {

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
        System.out.println(Timestamp.valueOf(LocalDateTime.now()));

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
            if (rows.next()) {
                String token = generateNemToken();
                User user = new User();
                user.setToken(token);
                user.setUsername((String)payload.get("username"));
                tokens.add(user);
                return "authToken: " + token;
            } else {
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
    public String createAuction(@RequestBody Map<String, Object> payload,
                                @RequestParam ("token") String token) {
        Connection conn = RestServiceApplication.getConnection();

        String user;
        if(findToken(token) == null){
            return "AuthError";
        }else{
            user = findToken(token);
        }

        try (PreparedStatement ps = conn.prepareStatement(""
                + "INSERT INTO vendedor_artigo (artigo_nome, artigo_precoinicial, artigo_datalimite, artigo_ean, utilizador_username)"
                + "         VALUES ( ? , ? , ? , ?, ?)")) {
            ps.setString(1, (String) payload.get("nome_artigo"));
            ps.setDouble(2, (Double) payload.get("preco_inicial"));

            int year = (int) payload.get("year");
            int month = (int) payload.get("month");
            int day = (int) payload.get("day");
            int hour = (int) payload.get("hour");
            int min = (int) payload.get("minute");
            LocalDateTime date = LocalDateTime.of(year, month, day, hour, min);
            Timestamp time = Timestamp.valueOf(date);

            ps.setTimestamp(3, time);
            ps.setLong(4, generateEAN());
            ps.setString(5, user);
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
    public List<Map<String, Object>> getAllLeiloes(@RequestParam ("token") String token) {
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> payload = new ArrayList<>();

        String user;
        if(findToken(token) == null){
            content.put("AuthError", "erro");
            payload.add(content);
            return payload;
        }else{
            user = findToken(token);
        }

        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT artigo_ean, artigo_nome FROM vendedor_artigo");
            logger.debug("---- vendedor_artigo  ----");
            while (rows.next()) {
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
    public Map<String, Object> getLeiloes(@PathVariable("keyword") String keyword,
                                          @RequestParam ("token") String token) {
        logger.info("###              GET /leiloes              ###");
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> content = new HashMap<>();
        String user;
        if(findToken(token) == null){
            content.put("AuthError", "erro");
            return content;
        }else{
            user = findToken(token);
        }
        if (keyword.length() == 13 && Long.parseLong(keyword) > 0) {
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
        } else {
            try (PreparedStatement ps = conn.prepareStatement("SELECT artigo_ean, artigo_nome FROM vendedor_artigo WHERE artigo_nome Like ?")) {
                ps.setString(1, "%" + keyword + "%");
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
    public Map<String, Object> getDetalhesLeilao(@PathVariable("leilaoId") long artigo_ean,
                                                 @RequestParam ("token") String token) {
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> content = new HashMap<>();
        String user;
        if(findToken(token) == null){
            content.put("AuthError", "erro");
            return content;
        }else{
            user = findToken(token);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM vendedor_artigo WHERE vendedor_artigo.artigo_ean = ?")) {
            ps.setLong(1, artigo_ean);
            ResultSet rows = ps.executeQuery();
            logger.debug("---- selected ean  ----");
            if (rows.next()) {

                logger.debug("'leilaoID': {}, 'descricao': {}, 'preco inicial': {}, 'preco final': {}, 'data limite': {}, 'vendedor':{}",
                        rows.getLong("artigo_ean"), rows.getString("artigo_nome"), rows.getDouble("artigo_precoinicial"),
                        rows.getDouble("artigo_precoatual"), rows.getTimestamp("artigo_dataLimite").toString(),
                        rows.getString("utilizador_username")
                );
                content.put("leilaoID", rows.getLong("artigo_ean"));
                content.put("descricao", rows.getString("artigo_nome"));
                content.put("preco inicial", rows.getDouble("artigo_precoinicial"));
                content.put("preco atual", rows.getDouble("artigo_precoatual"));
                content.put("data limite", rows.getTimestamp("artigo_datalimite"));
                content.put("vendedor", rows.getString("utilizador_username"));
            }
            try (PreparedStatement ps2 = conn.prepareStatement("SELECT comentario FROM mural WHERE artigo_ean = ?")){
                ps2.setLong(1, artigo_ean);
                ResultSet rows2 = ps2.executeQuery();
                int label;
                while (rows2.next()){
                    label = rows2.getRow();
                    content.put("comentario" + label, rows2.getString("comentario"));
                }
            } catch (SQLException ex1){
                logger.error("Error in DB", ex1);
            }

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
        return content;
    }

    /**
     * GET Listar todos os leilões em que o utilizador tenha atividade.
     *
     *
     * @return
     */
    @GetMapping(value = "/dbproj/listar/leiloes", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getListaLeiloesUsername(@RequestParam ("token") String token) {
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> content = new HashMap<>();
        String user;
        if(findToken(token) == null){
            content.put("AuthError", "erro");
            return content;
        }else{
            user = findToken(token);
        }
        try (PreparedStatement ps = conn.prepareStatement("Select artigo_ean, artigo_nome, artigo_precoatual, artigo_precoinicial, artigo_datalimite, artigo_terminado, utilizador_username as \"vendedor\" " +
                "from vendedor_artigo " +
                "where utilizador_username = ? or artigo_ean = (Select distinct artigo_ean from licitacao where comprador_username = ?);")) {
            ps.setString(1, user);
            ps.setString(2, user);
            ResultSet rows = ps.executeQuery();
            while (rows.next()) {
                content.put("leilaoID", rows.getLong("artigo_ean"));
                content.put("descricao", rows.getString("artigo_nome"));
                content.put("preco inicial", rows.getDouble("artigo_precoinicial"));
                content.put("preco atual", rows.getDouble("artigo_precoatual"));
                content.put("terminado", rows.getBoolean("artigo_terminado"));
                content.put("data limite", rows.getTimestamp("artigo_datalimite"));
                content.put("vendedor", rows.getString("vendedor"));
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
        return content; // returns empty if error or none selected
    }

    /**
     * GET CRIAR LICITACAO
     * se a data da licitacao (data atual) for maior que a data final do leilao
     * ou
     * se o valor da licitacao for menor que o valor atual do leilao
     * entao
     * nao aceita a licitacao
     */
    @GetMapping(value = "/dbproj/licitar/{leilaoID}/{licitacao}")
    @ResponseBody
    public String createAuction(@PathVariable("leilaoID") long leilaoID,
                                @PathVariable("licitacao") float licitacao,
                                @RequestParam("token") String token) {

        Connection conn = RestServiceApplication.getConnection();
        String username;
        if (findToken(token) == null) {
            return "authError";
        } else {
            username = findToken(token);
        }

        try (PreparedStatement ps = conn.prepareStatement("SELECT  MAX (valor)  "
                + "FROM licitacao "
                + "WHERE licitacao.artigo_ean = ?")) {
            ps.setLong(1, leilaoID);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                float max = resultSet.getFloat("max");
                if (max < licitacao) {
                    try (PreparedStatement checkData = conn.prepareStatement("SELECT artigo_datalimite " +
                            "FROM vendedor_artigo " +
                            "WHERE artigo_ean = ? ")) {
                        checkData.setLong(1, leilaoID);
                        ResultSet resultData = checkData.executeQuery();
                        if (resultData.next()) {
                            Timestamp data = resultData.getTimestamp("artigo_datalimite");
                            if (LocalDateTime.now().isBefore(data.toLocalDateTime())) {
                                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO licitacao (comprador_username, artigo_ean, valor )" +
                                        "VALUES (?, ?, ? );" +
                                        "UPDATE vendedor_artigo " +
                                        "SET artigo_precoatual = ? WHERE artigo_ean = ?")) {

                                    insertStmt.setString(1, username);
                                    insertStmt.setLong(2, leilaoID);
                                    insertStmt.setFloat(3, licitacao);
                                    insertStmt.setFloat(4, licitacao);
                                    insertStmt.setLong(5, leilaoID);
                                    int result = insertStmt.executeUpdate();
                                    conn.commit();
                                    if (result >= 1) {
                                        PreparedStatement check = conn.prepareStatement("SELECT DISTINCT comprador_username " +
                                                "FROM licitacao " +
                                                "WHERE artigo_ean = ?");
                                        check.setLong(1, leilaoID);
                                        ResultSet resultSet1 = check.executeQuery();
                                        while (resultSet1.next()) {
                                            PreparedStatement sendMessage = conn.prepareStatement("INSERT INTO mensagem (mensagem, utilizador_username) " +
                                                    "VALUES ( ?, ?)");
                                            sendMessage.setString(1, "Licitacao mais alta no artigo: " + leilaoID + " com o valor: " + licitacao);
                                            sendMessage.setString(2, resultSet1.getString("comprador_username"));
                                            sendMessage.executeUpdate();
                                            conn.commit();
                                        }
                                        return "Inserted!";
                                    }
                                }
                            }
                        }
                    }
                }
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
     * PUT EDITAR LEILAO
     *
     * @param leilaoId
     * @param token
     * @param payload
     * @return
     */
    @PutMapping(value = "/dbproj/leilao/{leilaoId}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String editarLeilao(@PathVariable("leilaoId") long leilaoId,
                                @RequestParam ("token") String token,
                                @RequestBody Map<String, Object> payload){

        Connection conn = RestServiceApplication.getConnection();
        String user;
        if(findToken(token) == null){
            return "AuthError";
        }else{
            user = findToken(token);
        }
        Timestamp datalimite;
        double precoinicial;
        double precoatual;

        try (PreparedStatement getLeilao = conn.prepareStatement("SELECT * FROM vendedor_artigo WHERE artigo_ean = ?")) {
            getLeilao.setLong(1, leilaoId);
            ResultSet resultgetLeilao = getLeilao.executeQuery();
            if (resultgetLeilao.next()) {
                datalimite = resultgetLeilao.getTimestamp("artigo_datalimite");
                precoinicial = resultgetLeilao.getDouble("artigo_precoinicial");
                precoatual = resultgetLeilao.getDouble("artigo_precoatual");
                try (PreparedStatement ps = conn.prepareStatement(""
                        + "INSERT INTO vendedor_artigo (artigo_nome, artigo_precoinicial, artigo_precoatual, artigo_datalimite, artigo_ean, utilizador_username)"
                        + "VALUES ( ? , ? , ? , ?, ?, ?);" +
                        "UPDATE vendedor_artigo SET artigo_terminado = 'True' WHERE artigo_ean = ?")) {
                    ps.setString(1, (String)payload.get("descricao"));
                    ps.setDouble(2, precoinicial);
                    ps.setDouble(3, precoatual);
                    ps.setTimestamp(4, datalimite);
                    ps.setLong(5, generateEAN());
                    ps.setString(6, user);
                    ps.setLong(7, leilaoId);

                    int affectedRows = ps.executeUpdate();
                    conn.commit();

                    if (affectedRows == 1) {
                        PreparedStatement ps2 = conn.prepareStatement("SELECT DISTINCT comprador_username FROM licitacao WHERE artigo_ean = ? AND valida = 'True' ");
                        ps2.setLong(1, leilaoId);
                        ResultSet rows = ps2.executeQuery();
                        int result = 0;
                        while (rows.next()){
                            PreparedStatement ps1 = conn.prepareStatement("INSERT INTO mensagem (mensagem, utilizador_username) " +
                                    "VALUES (?, ?)");
                            ps1.setString(1, "Este leilao foi editado: " + leilaoId);
                            ps1.setString(2, rows.getString("comprador_username"));
                            result += ps1.executeUpdate();
                            conn.commit();
                        }
                        if(result >=1){
                            return "inserted";
                        }
                        //TODO: enviar mensagem aos utilizadores
                        return "Inserted!";
                    }
                }
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
     * POST ADICIONAR COMENTARIO AO MURAL
     *
     * @param leilaoId EAN DO LEILAO EM QUESTAO
     * @param comentario ADICIONADO AO BODY
     * @return
     */
    @PostMapping(value = "/dbproj/leilao/mural/{leilaoID}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String escreverMural(@PathVariable("leilaoID") long leilaoId,
                                @RequestBody Map<String, Object> comentario,
                                @RequestParam ("token") String token){

        Connection conn = RestServiceApplication.getConnection();
        String user;
        if(findToken(token) == null){
            return "AuthError";
        }else{
            user = findToken(token);
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO mural (comentario, artigo_ean)" +
                "VALUES (?, ?, ?)")){
            ps.setString(1, (String) comentario.get("comentario"));
            ps.setLong(2, leilaoId);
            ps.setString(3, user);
            int affectedRows = ps.executeUpdate();
            conn.commit();
            if(affectedRows == 1){
                PreparedStatement ps2 = conn.prepareStatement("SELECT DISTINCT username, utilizador_username FROM mural, vendedor_artigo WHERE mural.artigo_ean = ? OR vendedor_artigo.artigo_ean = ?");
                ps2.setLong(1, leilaoId);
                ps2.setLong(2, leilaoId);
                ResultSet rows = ps2.executeQuery();
                int result = 0;
                while (rows.next()){
                    PreparedStatement ps1 = conn.prepareStatement("INSERT INTO mensagem (mensagem, utilizador_username) " +
                            "VALUES (?, ?)");
                    ps1.setString(1, "Nova mensagem no mural do artigo: " + leilaoId);
                    ps1.setString(2, rows.getString("username"));
                    result += ps1.executeUpdate();
                    conn.commit();
                }
                if(result >=1){
                    return "inserted";
                }
            }
        } catch (SQLException ex) {
            logger.error("error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }
        }finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return "failed";
    }

    /**
     * PUT TERMINAR LEILOES
     * @return
     */
    @PutMapping(value = "/dbproj/leiloes/terminar")
    @ResponseBody
    public String terminalLeiloes(){
        Connection conn = RestServiceApplication.getConnection();
        try(PreparedStatement ps = conn.prepareStatement("UPDATE vendedor_artigo " +
                "SET artigo_terminado = 'True' "+
                "WHERE artigo_datalimite >= ?")){
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            int res =  ps.executeUpdate();
            conn.commit();
            if(res >=1){
                PreparedStatement ps1 = conn.prepareStatement("SELECT artigo_precoatual, artigo_ean FROM vendedor_artigo " +
                        "WHERE artigo_terminado = 'True' AND vencedor = null ");
                ResultSet resultSet = ps1.executeQuery();
                while (resultSet.next()){
                    PreparedStatement ps2 = conn.prepareStatement("UPDATE vendedor_artigo " +
                            "SET vencedor = (SELECT comprador_username FROM licitacao " +
                            "WHERE artigo_ean = ? AND valor = ? AND valida = 'True') " +
                            "WHERE artigo_ean = ?");
                    ps2.setLong(1, resultSet.getLong("artigo_ean"));
                    ps2.setDouble(2, resultSet.getDouble("artigo_precoatual"));
                    ps2.setLong(3, resultSet.getLong("artigo_ean"));
                }
                return "done";
            }
        } catch (SQLException ex) {
            logger.error("error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }
        }finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return "failed";
    }

    @GetMapping(value = "/dbproj/user/inbox", produces = "application/json")
    @ResponseBody
    public Map<String, Object> printNotif(@RequestParam String token){
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> content = new HashMap<>();
        String user;
        if(findToken(token) == null){
            content.put("AuthError", "erro");
            return content;
        }else{
            user = findToken(token);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT mensagem FROM mensagem WHERE utilizador_username = ?")){
            ps.setString(1, user);
            ResultSet rows = ps.executeQuery();
            conn.commit();
            while (rows.next()){
                content.put("mensagem", rows.getString("mensagem"));
            }
        } catch (SQLException ex) {
            logger.error("error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }
        }finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }

        return content;
    }

}