 //控制层 
app.controller('contentController' ,function($scope,contentService){	
	
	$scope.contentList=[];
    //读取列表数据绑定到表单中  
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId]=response;
			}			
		);
	}
	
	$scope.search=function(keywords){
		location.href='http://localhost:9104/search.html#?keywords='+keywords;
	}
	
});	
