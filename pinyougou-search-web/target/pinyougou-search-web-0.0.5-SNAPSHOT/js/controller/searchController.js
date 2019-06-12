app.controller("searchController",function($scope,searchService){
	
	$scope.searchEntity={keywords:{}};
	$scope.searchItem=function(searchMap){
		
		searchService.searchItem(searchMap).success(
				function(response){
			
		});
	}
	
})