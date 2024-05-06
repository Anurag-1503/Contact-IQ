const toggleSidebar = () => {
	
	if($('.sidebar').is(":visible"))
	{
		//close it
		$('.sidebar').css("display" , "none");
		$('.content').css("margin-left","0%");
	}else{
		//show it
		$('.sidebar').css("display" , "block");
		$('.content').css("margin-left","20%");
	}
};
