package com.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.dao.UserRepository;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	// home page handler
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - ContactIQ");
		return "home";
	}

	// about page handler
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - ContactIQ");
		return "about";
	}

	// signup page handler
	@GetMapping("/signup")
	public String Signup(Model m) {
		m.addAttribute("title", "Register - ContactIQ");
		m.addAttribute("user", new User());
		return "Signup";
	}

	// handler for registerUser()
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {

			if (!agreement) {
				System.out.println("you have not agreed the terms and conditions");
				throw new Exception("you have not agreed the terms and conditions");
			}
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR " + result1.toString());
				model.addAttribute("user", user);
				return "Signup";
			}
				

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement " + agreement);
			System.out.println("User" + user);

			User result = this.userRepository.save(user);

			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("successfully registered " , "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("something went wrong !! " + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}
	
	//handler for custom login page
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title", "Login Page");
		return "login";
	}
}
