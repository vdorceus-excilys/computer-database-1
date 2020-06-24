package com.excilys.formation.cdb.persistence;

import static com.excilys.formation.cdb.persistence.UtilDAO.fermetureSilencieuse;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.excilys.formation.cdb.model.Computer;
import com.excilys.formation.cdb.model.Page;
import com.excilys.formation.cdb.persistence.mapper.ComputerMapper;

public class ComputerDAO {
    private static ComputerDAO computerDAO;
    private Connection connect = MyConnect.getConnection();

    private static final String SELECT_ALL = "SELECT id, name, introduced, discontinued, company_id FROM computer ORDER BY id";
    private static final String COUNT = "SELECT COUNT(id) AS nb_computer FROM computer";
    private static final String SELECT_BY_ID = "SELECT id, name, introduced, discontinued, company_id FROM computer WHERE computer.id = ?";
    private static final String INSERT = "INSERT INTO computer (name, introduced, discontinued, company_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE computer SET name = ?, introduced = ?, discontinued = ?, company_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM computer where id = ?";
    private static final String SELECT_WITH_PAGE = "SELECT id, name, introduced, discontinued, company_id FROM computer ORDER BY id LIMIT ? OFFSET ?";

    /**
     * L'instance du singleton de ComputerDAO.
     * 
     * @return l'instance de OrdinateurDAO
     */
    public static ComputerDAO getInstance() {
	if (computerDAO == null) {
	    computerDAO = new ComputerDAO();
	}
	return computerDAO;
    }

    /**
     * Lister tous les ordinateurs
     * 
     * @return Liste tous les ordinateurs
     */
    public List<Computer> getAll() {
	List<Computer> computerList = new ArrayList<Computer>();
	PreparedStatement statement = null;
	ResultSet resultSet = null;

	try {
	    statement = connect.prepareStatement(SELECT_ALL);
	    resultSet = statement.executeQuery();

	    while (resultSet.next()) {
		Computer computer = ComputerMapper.map(resultSet);
		computerList.add(computer);
	    }
	} catch (SQLException e) {
	    System.err.println("Erreur DAO -> Lister tous les ordinateurs" + e.getMessage());
	} finally {
	    fermetureSilencieuse(resultSet);
	    fermetureSilencieuse(statement);
	}
	return computerList;
    }

    /**
     * Compter le nombre total d'ordinateurs.
     * 
     * @return le nombre total d'ordinateurs.
     */
    public int countAll() {
	int result = 0;
	PreparedStatement statement = null;
	ResultSet resultSet = null;

	try {
	    statement = connect.prepareStatement(COUNT);
	    resultSet = statement.executeQuery();

	    while (resultSet.next()) {
		result = resultSet.getInt("nb_computer");
	    }
	    System.out.println("Nombre total d'entrées dans la base : " + result);
	} catch (SQLException e) {
	    System.err.println("Erreur DAO -> Compter tous les ordinateurs");
	} finally {
	    fermetureSilencieuse(resultSet);
	    fermetureSilencieuse(statement);
	}
	return result;
    }

    /**
     * Recherche un ordinateur par un identifiant
     * 
     * @param id l'identifiant à chercher en base
     * @return L'ordinateur correspondant si l'identifiant est présent, sinon null.
     */
    public Computer findById(Long id) {
	Computer computer = null;
	PreparedStatement statement = null;
	ResultSet resultSet = null;

	if (id != null) {
	    try {
		statement = connect.prepareStatement(SELECT_BY_ID);
		statement.setLong(1, id);
		resultSet = statement.executeQuery();

		while (resultSet.next()) {
		    computer = ComputerMapper.map(resultSet);
		}
	    } catch (SQLException e) {
		System.err.println("Erreur DAO -> Ordinateur par id : " + e.getMessage());
	    } finally {
		fermetureSilencieuse(resultSet);
		fermetureSilencieuse(statement);
	    }
	}
	return computer;
    }

    /**
     * Création d'un nouvel ordinateur
     * 
     * @param computer l'ordinateur à insérer en base
     */
    public void create(Computer computer) {
	PreparedStatement statement = null;

	if (computer != null) {
	    try {
		statement = connect.prepareStatement(INSERT);
		statement.setString(1, computer.getName());
		Date introducedDate = computer.getIntroduced() == null ? null : computer.getIntroduced();
		statement.setDate(2, introducedDate);
		Date discontinuedDate = computer.getDiscontinued() == null ? null : computer.getDiscontinued();
		statement.setDate(3, discontinuedDate);
		Long idCompany = computer.getIdCompany();
		if (idCompany != null) {
		    statement.setLong(4, idCompany);
		}
		statement.execute();
	    } catch (SQLException e) {
		System.err.println(
			"Erreur DAO -> insertion ordinateur. Vérifiez que l'id pour l'entreprise" + e.getMessage());
	    } finally {
		fermetureSilencieuse(statement);
	    }
	}
    }

    /**
     * Mise à jour d'un ordinateur
     * 
     * @param computer l'ordinateur à mettre à jour en base
     */
    public void update(Computer computer) {
	PreparedStatement statement = null;

	if (computer != null) {
	    try {
		statement = connect.prepareStatement(UPDATE);
		statement.setString(1, computer.getName());
		Date introducedDate = computer.getIntroduced() == null ? null : computer.getIntroduced();
		statement.setDate(2, introducedDate);
		Date discontinuedDate = computer.getDiscontinued() == null ? null : computer.getDiscontinued();
		statement.setDate(3, discontinuedDate);
		statement.setLong(4, computer.getIdCompany());
		statement.setLong(5, computer.getId());
		statement.execute();
	    } catch (SQLException e) {
		System.err.println("Erreur DAO -> mise a jour ordinateur" + e.getMessage());
	    } finally {
		fermetureSilencieuse(statement);
	    }
	}
    }

    /**
     * Suppression d'un ordinateur
     * 
     * @param id l'identifiant de l'ordinateur à supprimer en base
     */
    public void delete(Long id) {
	PreparedStatement statement = null;

	try {
	    statement = connect.prepareStatement(DELETE);
	    statement.setLong(1, id);
	    statement.execute();
	} catch (SQLException e) {
	    System.err.println("Erreur DAO -> suppression ordinateur" + e.getMessage());
	} finally {
	    fermetureSilencieuse(statement);
	}
    }

    /**
     * Liste tous les ordinateurs dans une page spécifique
     * 
     * @param p les informations qui doivent être contenues dans la page p
     * @return la liste d'ordinateurs
     */
    public List<Computer> getByPage(Page p) {
	List<Computer> computers = new ArrayList<Computer>();
	PreparedStatement statement = null;
	ResultSet resultSet = null;

	try {
	    statement = connect.prepareStatement(SELECT_WITH_PAGE);

	    statement.setInt(1, p.getRows());
	    statement.setInt(2, p.getFirstLine());

	    resultSet = statement.executeQuery();
	    while (resultSet.next()) {
		Computer computer = ComputerMapper.map(resultSet);
		computers.add(computer);
	    }
	} catch (SQLException e) {
	    System.err
		    .println("Erreur DAO -> liste des ordinateurs de la page : " + p.getCurrentPage() + e.getMessage());
	} finally {
	    fermetureSilencieuse(resultSet);
	    fermetureSilencieuse(statement);
	}
	return computers;
    }

    public void closeConnect() {
	if (connect != null) {
	    try {
		connect.close();
	    } catch (SQLException e) {
		System.out.println("Échec de la fermeture de la connexion : " + e.getMessage());
	    }
	}
    }
}
