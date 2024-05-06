package com.contact.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepository;
import com.contact.dao.UserRepository;
import com.contact.entities.Contact;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import services.SessionHelper;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		/* System.out.println("USER NAME : " + userName); */
		// get the user using username(email)
		User user = this.userRepository.getUserByUsername(userName);
		/* System.out.println("USER : " + user); */

		model.addAttribute("user", user);
	}

	// dashboard home
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open Add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing addContact Form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") @Valid Contact contact, BindingResult bindingResult,
			Principal principal, Model model, @RequestParam("image") MultipartFile file, HttpSession session,
			SessionHelper sessionHelper) {

		try {

			String name = principal.getName();
			User user = this.userRepository.getUserByUsername(name);

			// processing and uploading file

			if (file.isEmpty()) {
				// send some message
				System.out.println("File is empty");
				contact.setImage("default_bot.png");
			} else {
				// upload the file to folder and update the name to

				// setting image
				contact.setImage(file.getOriginalFilename());
				// getting path where image will be uploaded
				File saveFile = new ClassPathResource("static/img").getFile();
				// getting URL/path of the uploaded image
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				// saving it
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");

			}

			user.getContacts().add(contact);

			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("DATA " + contact);

			System.out.println("ADDED TO DATABASE");

			// success message
			session.setAttribute("message", new Message("your contact is added!!!!!!!!!!", "success"));

			System.out.println(contact);

		}

		catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());

			// error message
			session.setAttribute("message", new Message("Something went wrong", "danger"));
		}

		return "normal/add_contact_form";
	}

	// view contacts handler
	// per page 5 contacts
	@GetMapping("/show-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "view user contacts");

		// send contact list to show contact page
		String userName = principal.getName();
		User user = this.userRepository.getUserByUsername(userName);

		Pageable pageable = PageRequest.of(page, 4);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// handler for showing specific contact detail
	@GetMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal principal) {
		System.out.println("CID : " + cid);
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		// check
		String userName = principal.getName();
		User user = this.userRepository.getUserByUsername(userName); // this is the currently logged in user

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// handler for deleting contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Principal principal, HttpSession session) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		User user = this.userRepository.getUserByUsername(principal.getName());
		// check
		System.out.println("Contact " + contact.getCid());

		if (user.getId() == contact.getUser().getId()) {
			contact.setUser(null);

			// remove photo
			/*
			 * String image = contact.getImage(); if (!image.equals("default.png") &&
			 * !image.isEmpty()) {
			 * 
			 * // Delete the image file try { File deleteFile = new
			 * ClassPathResource("static/img").getFile(); String imagePath =
			 * deleteFile+File.separator+ image;
			 * 
			 * Files.deleteIfExists(Paths.get(imagePath)); } catch (IOException e) {
			 * 
			 * 
			 * session.setAttribute("message", new
			 * Message("Something went wrong! "+e.getMessage(),"danger"));
			 * 
			 * return "redirect:/user/show-contact/0"; } }
			 */

			this.contactRepository.deleteByIdCustom(cid);
			/*
			 * session.setAttribute("message", new
			 * Message("Contact deleted successfully","success"));
			 */
		} else {
			/*
			 * session.setAttribute("message", new
			 * Message("You dont have permission do to this","danger"));
			 */
		}

		// redirect
		return "redirect:/user/show-contact/0";
	}
	
	//handler for open update contact form
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid ,  Model model) {
		
		model.addAttribute("title", "update contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//handler for updating contact
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file ,
			Model model,
			HttpSession session,
			Principal principal) {
		
		try {
			
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();
			
			if(!file.isEmpty()) {
				/* rewrite */
				
				//if new image is selected by user , first delete old photo 
//				File deleteFile = new ClassPathResource("static/img").getFile();
//				File file1 = new File(deleteFile, oldContactDetail.getImage());
//				file1.delete();
				
//				other users having same profile pic will also be deleted so i skipped this step
				
				
				
				//then set new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			}
			else {
				//if no new image is selected by user , just set the last image
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUsername(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
//			session.setAttribute("message", new Message("Your contact is updated" , "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Contact : " +contact.getName());
		System.out.println("Contact : " +contact.getCid());

		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
}
