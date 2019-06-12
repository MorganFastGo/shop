app.controller("searchController",function($scope,searchService,$location){
	
	$scope.searchEntity={keywords:'',category:'',brand:'',spec:{},price:'',currentPage:1,rows:40,sortValue:'',sortField:''};
	$scope.resultEntity={itemList:[],categoryList:[],brandList:[],specList:[],totalPages:'',totalElements:''};
	$scope.searchItem=function(){
		$scope.searchEntity.currentPage=parseInt($scope.searchEntity.currentPage);
		searchService.searchItem($scope.searchEntity).success(
				function(response){
					$scope.resultEntity.itemList=response.itemList;
					$scope.resultEntity.categoryList=response.categoryList;
					$scope.resultEntity.brandList=response.brandList;
					$scope.resultEntity.specList=response.specList;
					$scope.resultEntity.totalPages=response.totalPages;
					$scope.resultEntity.totalElements=response.totalElements;//总记录条数
				
					$scope.keyWordsIsBrand();
					
					$scope.setPages($scope.searchEntity.currentPage);
		});
	}
	$scope.initKeywords=function(){
		$scope.searchEntity.keywords=$location.search()['keywords'];
		$scope.searchItem();
	}
	
	$scope.keyWordsIsBrand=function(){
		for (var i = 0; i < $scope.resultEntity.brandList.size(); i++) {
			if($scope.searchEntity.keywords.indexOf($scope.resultEntity.brandList[i].text)>=0){
				$scope.searchEntity.brand=$scope.resultEntity.brandList[i].text;
			}
		}
		
	}
	
	$scope.addSearchElement=function(key,value){
		if('brand'==key||'category'==key||'price'==key){
			$scope.searchEntity[key]=value;
		}else{
			$scope.searchEntity.spec[key]=value;
		}
		
		$scope.searchItem();
	
	}
	$scope.deleSearchElement=function(key){
		if('brand'==key||'category'==key){
			$scope.searchEntity[key]='';
		}else{
			delete $scope.searchEntity.spec[key];
		}
		$scope.searchItem();
	}
		
	$scope.setPages=function(currentPage){
		var startPage;
		var endPage;
		if($scope.resultEntity.totalPages<=5){
			startPage=1;
			endPage=$scope.resultEntity.totalPages;
		
		}else{
			startPage=currentPage-2;
			endPage=currentPage+2;
			if(startPage<=0){
				startPage=1;
				endPage=5;
			}
			if(endPage>$scope.resultEntity.totalPages){
				startPage=$scope.resultEntity.totalPages-4;
				endPage=$scope.resultEntity.totalPages;
			}
		}
		
		$scope.pageList=[];
		for(var i=startPage;i<=endPage;i++){
			$scope.pageList.push(i);
		}
		
	}
	
	$scope.setCurrentPage=function(currentPage){
		if(currentPage<=0){
			currentPage=1;
		}
		if(currentPage>$scope.resultEntity.totalPages){
			currentPage=$scope.resultEntity.totalPages;
		}
		$scope.searchEntity.currentPage=currentPage;
	}
	
	$scope.setSort=function(sortValue,sortField){
		$scope.searchEntity.sortValue=sortValue;
		$scope.searchEntity.sortField=sortField;
	}
})