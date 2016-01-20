package com.springboot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.springboot.models.Employee;

@Controller
public class ApplicationController {
	
	private static final Logger log = LoggerFactory
			.getLogger(ApplicationController.class);

	@Autowired(required = false)
	DataSource dataSource;
	@Autowired
	ApplicationInstanceInfo instanceInfo;
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired(required = false) RedisConnectionFactory redisConnectionFactory;
	@Autowired(required = false) MongoDbFactory mongoDbFactory;

	@RequestMapping("/")
	public String home(Model model) {
		log.info("Controller called for path //");
		
		Map<Class<?>, String> services = new LinkedHashMap<Class<?>, String>();
		services.put(dataSource.getClass(), toString(dataSource));
		 services.put(redisConnectionFactory.getClass(), toString(redisConnectionFactory));
		 //services.put(mongoDbFactory.getClass(), toString(mongoDbFactory));
		model.addAttribute("services", services.entrySet());

		Map<String, Object> instancePropMap = instanceInfo.getProperties();
		instancePropMap.remove("application_version");
		instancePropMap.remove("application_id");
		instancePropMap.remove("instance_id");
		instancePropMap.remove("instance_index");
		instancePropMap.remove("uris");
		instancePropMap.remove("version");

		model.addAttribute("instanceInfo", instanceInfo);
		model.addAttribute("employee", new Employee());

		return "welcomepage";
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String listPersons(Model model) {	
		log.info("Controller called for path /home ");
		
		model.addAttribute("employee", new Employee());
		model.addAttribute("listEmployees", getAll());
		return "home";
	}
	
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public String gotoMainPage(Model model) {	
		log.info("Controller called for path /home ");
		
		model.addAttribute("employee", new Employee());
		model.addAttribute("listEmployees", getAll());
		return "main";
	}

	// For add and update person both
	@RequestMapping(value = "/employee/add", method = RequestMethod.POST)
	public String addPerson(@ModelAttribute("employee") Employee p) {

		if (p.getId() == 0) {
			// new person, add it
			save(p);
		} else {
			// existing person, call update
			update(p);
		}

		return "redirect:/home";

	}

	@RequestMapping("/remove/{id}")
	public String removePerson(@PathVariable("id") String id) {
		log.info("Controller called for removing person with ID "+id);
		deleteById(Integer.parseInt(id));
		return "redirect:/home";
	}

	@RequestMapping("/edit/{id}")
	public String editPerson(@PathVariable("id") String id, Model model) {
		log.info("Controller called for editing person with ID "+id);
		model.addAttribute("employee", getById(Integer.parseInt(id)));
		model.addAttribute("listEmployees", getAll());
		return "home";
	}

	private void save(Employee employee) {

		String query = "insert into Employee (name, role) values (?,?)";
		log.info("Executing query :: "+query);
		Object[] args = new Object[] { employee.getName(),
				employee.getRole() };

		int out = jdbcTemplate.update(query, args);

		if (out != 0) {
			log.info("Employee saved with id=" + employee.getId());
		} else
			log.info("Employee save failed with id="
					+ employee.getId());

	}

	private Employee getById(int id) {
		String query = "select id, name, role from Employee where id = ?";
		log.info("Executing query :: "+query);
		// using RowMapper anonymous class, we can create a separate RowMapper
		// for reuse
		Employee emp = jdbcTemplate.queryForObject(query, new Object[] { id },
				new RowMapper<Employee>() {

					public Employee mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						Employee emp = new Employee();
						emp.setId(rs.getInt("id"));
						emp.setName(rs.getString("name"));
						emp.setRole(rs.getString("role"));
						return emp;
					}
				});

		return emp;
	}

	private void update(Employee employee) {
		String query = "update Employee set name=?, role=? where id=?";
		log.info("Executing query :: "+query);
		Object[] args = new Object[] { employee.getName(), employee.getRole(),
				employee.getId() };

		int out = jdbcTemplate.update(query, args);
		if (out != 0) {
			log.info("Employee updated with id=" + employee.getId());
		} else
			log.info("No Employee found with id=" + employee.getId());
	}

	private void deleteById(int id) {

		String query = "delete from Employee where id=?";
		log.info("Executing query :: "+query);

		int out = jdbcTemplate.update(query, id);
		if (out != 0) {
			log.info("Employee deleted with id=" + id);
		} else
			log.info("No Employee found with id=" + id);
	}

	public List<Employee> getAll() {
		String query = "select id, name, role from Employee";
		log.info("Executing query :: "+query);

		List<Employee> empList = new ArrayList<Employee>();

		List<Map<String, Object>> empRows = jdbcTemplate.queryForList(query);

		for (Map<String, Object> empRow : empRows) {
			Employee emp = new Employee();
			emp.setId(Integer.parseInt(String.valueOf(empRow.get("id"))));
			emp.setName(String.valueOf(empRow.get("name")));
			emp.setRole(String.valueOf(empRow.get("role")));
			empList.add(emp);
		}
		
		if(empList.isEmpty()){
			log.info("No record to fetch");
		}
		return empList;
	}

	private String toString(DataSource dataSource) {
		if (dataSource == null) {
			return "<none>";
		} else {
			try {
				Field urlField = ReflectionUtils.findField(
						dataSource.getClass(), "url");
				ReflectionUtils.makeAccessible(urlField);
				return stripCredentials((String) urlField.get(dataSource));
			} catch (Exception fe) {
				try {
					Method urlMethod = ReflectionUtils.findMethod(
							dataSource.getClass(), "getUrl");
					ReflectionUtils.makeAccessible(urlMethod);
					return stripCredentials((String) urlMethod.invoke(
							dataSource, (Object[]) null));
				} catch (Exception me) {
					return "<unknown> " + dataSource.getClass();
				}
			}
		}
	}

	private String stripCredentials(String urlString) {
		try {
			if (urlString.startsWith("jdbc:")) {
				urlString = urlString.substring("jdbc:".length());
			}
			URI url = new URI(urlString);
			return new URI(url.getScheme(), null, url.getHost(), url.getPort(),
					url.getPath(), null, null).toString();
		} catch (URISyntaxException e) {
			log.error("Error occured while stripping credentials ",e);
			return "<bad url> " + urlString;
		}
	}
	
	 private String toString(MongoDbFactory mongoDbFactory) {
	        if (mongoDbFactory == null) {
	            return "<none>";
	        } else {
	            try {
	                return mongoDbFactory.getDb().getMongo().getAddress().toString();
	            } catch (Exception ex) {
	                return "<invalid address> " + mongoDbFactory.getDb().getMongo().toString();
	            }
	        }
	    }

	    private String toString(RedisConnectionFactory redisConnectionFactory) {
	        if (redisConnectionFactory == null) {
	            return "<none>";
	        } else {
	            if (redisConnectionFactory instanceof JedisConnectionFactory) {
	                JedisConnectionFactory jcf = (JedisConnectionFactory) redisConnectionFactory;
	                return jcf.getHostName().toString() + ":" + jcf.getPort();
	            } else if (redisConnectionFactory instanceof LettuceConnectionFactory) {
	                LettuceConnectionFactory lcf = (LettuceConnectionFactory) redisConnectionFactory;
	                return lcf.getHostName().toString() + ":" + lcf.getPort();
	            }
	            return "<unknown> " + redisConnectionFactory.getClass();
	        }
	    }
}
