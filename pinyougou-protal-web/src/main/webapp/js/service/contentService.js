//服务层
app.service('contentService',function($http){
	    	

	//查询实体
	this.findByCategoryId=function(categoryId){
		return $http.get('./content/findByCategoryId.do?categoryId='+categoryId);
	}
   	
});
