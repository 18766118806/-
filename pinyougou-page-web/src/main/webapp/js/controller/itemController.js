app.controller('itemController',function($scope){
//数量操作
$scope.addNum=function(x){
$scope.num+=x;
if($scope.num<1){
$scope.num=1;
}
}
$scope.specificationItems = {};  //记录用户选择的规格


$scope.selectSpecification = function(key,value){
	$scope.specificationItems[key] = value;
	$scope.searchSku();
}

// //判断某规格选项是否被用户选中
$scope.isSelected=function(name,value){
if($scope.specificationItems[name]==value){
	return true;
}else{
	return false;
} 
}  
//加载默认sku
$scope.loadSkuList = function (){

	$scope.sku = skuList[0];
	
	$scope.specificationItems =  JSON.parse(JSON.stringify(skuList[0].spec));
}


//比较两个对象是否相等

$scope.matchObject  =function(map1,map2){
	for(var key in map1){
		if(map1[key]!=map2[key]){
			return false;
		}
	}
	
	for(var key in map2){
		if(map2[key]!=map1[key]){
			return false;
		}
	}
	return true;
}



$scope.searchSku = function(){
	for(var i = 0;i<skuList.length;i++){
		if($scope.matchObject(skuList[i].spec,$scope.specificationItems)){
			$scope.sku  = skuList[i];
			return;
		}
	}
	$scope.sku = {"title":"----不存在----","price":"0.00"}
}


//添加到购物车


$scope.addToCat = function(){
	alert("添加成功"+$scope.sku.id)
}
});