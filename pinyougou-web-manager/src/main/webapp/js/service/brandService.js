// 定义服务层:
app.service("brandService",function($http){
	this.findAll = function(){
		return $http.get("../brand/findAll");
	}
	
	this.findPage = function(page,rows){
		return $http.get("../brand/findPage?pageNum="+page+"&pageSize="+rows);
	}
	
	this.add = function(entity){
		return $http.post("../brand/add",entity);
	}
	
	this.update=function(entity){
		return $http.post("../brand/update",entity);
	}
	
	this.findOne=function(id){
		return $http.get("../brand/findOne?id="+id);
	}
	
	this.dele = function(ids){
		return $http.get("../brand/deletes?ids="+ids);
	}
	
	this.search = function(page,rows,searchEntity){
		return $http.post("../brand/search?pageNum="+page+"&pageSize="+rows,searchEntity);
	}
	
	this.selectOptionList = function(){
		return $http.get("../brand/selectOptionList");
	}
});