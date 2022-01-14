package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
 
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME" +userName);
		
		//get the user using username(email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("user"+ user);
		model.addAttribute("user", user);
	}
	
	
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
	
	
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	//add contact  open form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileimage") MultipartFile file, Principal principal,HttpSession session )
	{
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading file...
		
		if(file.isEmpty())
		{
			//if the file is empty then try to give a message
			System.out.println("file is empty");
			contact.setImage("contact.png")	;	}
		else {
			//file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
		    System.out.println("image is uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		
		
		this.userRepository.save(user);
		System.out.println("Data "+contact);
		System.out.println("Added to data base");
		
		//alert success message........
		session.setAttribute("message", new Message("Contact added","success") );
		
		
		}
		catch(Exception e)
		{
		 System.out.println("ERROR"+e.getMessage())	;
		 e.printStackTrace();
		 //alert danger message
		 session.setAttribute("message", new Message("Something went wrong !! try Again","danger") );
		 
		}
		return "normal/add_contact_form";
	}
	
	
	//view contacts handler
	//per page =5
	//current page =0
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m,Principal principal)
	{
		m.addAttribute("title","user contacts");
		//send contact list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		//currentPage - page
		//Contact Per Page - 5
		 Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> findContactsByUser = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("findContactsByUser",findContactsByUser);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPage", findContactsByUser.getTotalPages());
		return "normal/show_contacts";
	}
	
	//showing particular contact details...
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
		System.out.println("CID" +cId);
		Optional<Contact> findById = this.contactRepository.findById(cId);
		Contact contact = findById.get();
		String userName = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(userName);
		//
		if(userByUserName.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_details";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal)
	{
		Optional<Contact> findById = this.contactRepository.findById(cId);
		Contact contact = findById.get();
		
//		contact.setUser(null);
//		this.contactRepository.delete(contact);
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message",new Message("contact deleted successfull...","success"));
		return"redirect:/user/show_contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cid,Model m)
	{
		m.addAttribute("title", "Update Contact");
		Optional<Contact> findByIds = this.contactRepository.findById(cid);
		Contact contact = findByIds.get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	
	//update contact handler
	@RequestMapping(value = "/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileimage") MultipartFile file,Model m,HttpSession session,Principal principal)
	{
		try {
			//old contact details
			Contact oldcontactDetails = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty())
			{
				//file work..
				//rewrite..
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetails.getImage());
				file1.delete();
				
				
				
				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldcontactDetails.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated","success"));
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("contactName "+contact.getName());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) 
	{
		model.addAttribute("title", "profile");
		return "normal/profile";
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSetting()
	{
		return "normal/settings";
	}
	
	//change password handler....
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
	{
		System.out.println("oldpassword" +oldPassword);
		System.out.println("newpassword" +newPassword);
		
		String userName=principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password is updated","success"));
        }
		else
		{	//something error
			session.setAttribute("message", new Message("Wrong old password!!","danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	
	
}
