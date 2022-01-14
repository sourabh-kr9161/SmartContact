package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;
	
 //email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	//send otp handler
	@RequestMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session)
	{
		System.out.println("EMAIL" +email);
		//generating otp of 4 digit...
		//Random random = new Random(1000);
		int otp = random.nextInt(9999);
		System.out.println("OTP"+otp);
		
		//write code for send otp to email..
		String subject="OTP From SCM";
		String message=""
				+"<div style='border:1px solid #e2e2e2; padding:20px'>"
				+"<h1>"
				+"OTP is"
				+"<b>"+otp
				+"</b>"
				+"</h1>"
				+"</div>";
		String to=email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag)
		{
			session.setAttribute("MyOtp", otp);
			session.setAttribute("email", email);
			return "verify_OTP";
			
		}
		else 
		{
			session.setAttribute("message", "Check your email id!!");
			return "forgot_email_form";
		}
		
	}
	//verify otp handler
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
	{
		int MyOtp=(int)session.getAttribute("MyOtp");
		String email=(String)session.getAttribute("email");
		if( MyOtp==otp)
		{
			//show password change form
			User user = this.userRepository.getUserByUserName(email);
			if(user==null)
			{
				//send error message
				session.setAttribute("message", "User does not exits with this email!!");
				return "forgot_email_form";
			}
			else {
				//send change password form
			}
			return "password_change_form";
		}
		else {
			session.setAttribute("message", "You have entered wrong otp!!");
			return "verify_OTP";
		}
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
	{
		String email=(String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		return "redirect:/signin?change=password changed successfully...";
	}
	
	
	
}
