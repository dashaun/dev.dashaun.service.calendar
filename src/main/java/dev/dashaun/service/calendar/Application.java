package dev.dashaun.service.calendar;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@SpringBootApplication
@EnableRedisRepositories
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}


@RedisHash
record Event (
		@NotNull
		@Id 
		UUID id, 
		
		@NotNull 
		String name, 
		
		@NotNull 
		@DateTimeFormat(iso= DateTimeFormat.ISO.DATE) 
		Date startDate, 
		
		@NotNull 
		@DateTimeFormat(iso= DateTimeFormat.ISO.DATE) 
		Date endDate, 
		
		String url,
		
		String content){}

@RestController()
@RequestMapping("/api/events")
class EventController {
	
	private final EventRepository eventRepository;
	

	EventController(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@GetMapping(value = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
	public List<Event> getEvents(){
		return StreamSupport.stream(eventRepository.findAll().spliterator(), false).toList();
	}

	@PostMapping(value = "/",
			produces = {MediaType.APPLICATION_JSON_VALUE},
			consumes = {MediaType.APPLICATION_JSON_VALUE})
	public Event createEvent(@RequestBody Event event){
		return eventRepository.save(event);
	}

	@GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public Optional<Event> getEventById(@PathVariable("id") String id){
		return eventRepository.findById(id);
	}
	
	@DeleteMapping("/{id}")
	public void deleteEvent(@PathVariable("id") String id){
		eventRepository.deleteById(id);
	}
}

interface EventRepository extends CrudRepository<Event, String>{}
