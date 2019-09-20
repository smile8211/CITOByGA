/**   
 * 画UML中的类节点MAP集合   
 * 依赖joint.js
 *    
 * @author JQH   
 * @date 2019-06-2  
 */   
function drawClass(uml,classList) {

	var classesMAP=new Map();
	var cx=70;
	var cy=50;
	for(var i=0;i<classList.length;i++){
		var cwidth=200;
		var cheight=100;
		if(i>0&&i%4===0){
			cx=70;
			cy=cy+350;
		}else if(i>0){
			cx=cx+250;
		}
		//评估宽高
		var cp=classList[i].attributes.length+classList[i].methods.length;
		//console.log(cp);
		if(cp>5){
			cheight=20*cp;
		}
		if(classList[i].type==='class'){
		//console.log("x:"+cx+"<===>y:"+cy);
		
		var oneclass=new uml.Class({
				position: { x:cx  , y: cy },
				size: { width: 200, height: cheight },
				name: classList[i].name,
				attributes: classList[i].attributes,
				methods: classList[i].methods,
				attrs: {
						'.uml-class-name-rect': {
								fill: '#ff8450',
								stroke: '#fff',
								'stroke-width': 0.5,
						},
						'.uml-class-attrs-rect': {
								fill: '#fe976a',
								stroke: '#fff',
								'stroke-width': 0.5
            			},
            			'.uml-class-methods-rect': {
                			fill: '#fe976a',
                			stroke: '#fff',
                			'stroke-width': 0.5
            			},
            			'.uml-class-attrs-text': {
            			    ref: '.uml-class-attrs-rect',
            			    'ref-y': 0.5,
            			    'y-alignment': 'middle'
						},
            			'.uml-class-methods-text': {
            			    ref: '.uml-class-methods-rect',
            			    'ref-y': 0.5,
            			    'y-alignment': 'middle'
            			}
        			}
    			});
		classesMAP.put(classList[i].name,oneclass);
		}
		if(classList[i].type==='Abstract'){
			//console.log("x:"+cx+"<===>y:"+cy);
			var oneAbstract=new uml.Abstract({
				position: { x:cx  , y: cy },
				size: { width: 200, height: cheight },
				name: classList[i].name,
				attributes: classList[i].attributes,
				methods: classList[i].methods,
        		attrs: {
        		    '.uml-class-name-rect': {
        		        fill: '#68ddd5',
        		        stroke: '#ffffff',
        		        'stroke-width': 0.5
        		    },
        		    '.uml-class-attrs-rect': {
        		        fill: '#9687fe',
        		        stroke: '#fff',
        		        'stroke-width': 0.5
        		    },
        		    '.uml-class-methods-rect': {
        		        fill: '#9687fe',
        		        stroke: '#fff',
        		        'stroke-width': 0.5
        		    },
        		    '.uml-class-methods-text, .uml-class-attrs-text': {
        		        fill: '#fff'
        		    }
        		}
    		});
			classesMAP.put(classList[i].name,oneAbstract);
		}
		if(classList[i].type==='Interface'){
			//console.log("x:"+cx+"<===>y:"+cy);
		var oneInterface=new uml.Interface({
				position: { x:cx  , y: cy },
				size: { width: 200, height: cheight },
				name: classList[i].name,
				attributes: classList[i].attributes,
				methods: classList[i].methods,
        		attrs: {
        		    '.uml-class-name-rect': {
        		        fill: '#feb662',
        		        stroke: '#ffffff',
        		        'stroke-width': 0.5
        		    },
        		    '.uml-class-attrs-rect': {
                		fill: '#fdc886',
            		    stroke: '#fff',
            		    'stroke-width': 0.5
            		},
            		'.uml-class-methods-rect': {
            		    fill: '#fdc886',
            		    stroke: '#fff',
            		    'stroke-width': 0.5
            		},
            		'.uml-class-attrs-text': {
            		    ref: '.uml-class-attrs-rect',
            		    'ref-y': 0.5,
            		    'y-alignment': 'middle'
            		},
            		'.uml-class-methods-text': {
            		    ref: '.uml-class-methods-rect',
            		    'ref-y': 0.5,
            		    'y-alignment': 'middle'
            		}
        		}
    		});
			classesMAP.put(classList[i].name,oneInterface);
		}
	}
	return classesMAP.data;
}
/**   
 * 画UML中的线集合   
 * 依赖joint.js
 *    
 * @author JQH   
 * @date 2019-06-2  
 */ 
function drawRelations(uml,links,classMap) {
	var relations=new Array();
	for(var i=0;i<links.length;i++){
		var sourceID;
		var targetID;
		for(var key in classMap){
			if(key===links[i].source){
				sourceID=classMap[key].id;
			}
			if(key===links[i].target){
				targetID=classMap[key].id;
			}
		}
		//console.log(sourceID+">>>>>>>>>>>>>"+targetID);
		//泛化（继承）关系Generalization（实线+白三角箭头）
		if(links[i].relationship==='Generalization'){
			var onerelation=new uml.Generalization({ source: { id: sourceID }, target: { id: targetID }});
			relations.push(onerelation);
		}
		//实现关系Implementation（虚线+白三角箭头）
		if(links[i].relationship==='Implementation'){
			var onerelation=new uml.Implementation({ source: { id: sourceID }, target: { id: targetID }});
			relations.push(onerelation);
		}
		//聚合关系Aggregation（实线+白菱形）
		if(links[i].relationship==='Aggregation'){
			var onerelation=new uml.Aggregation({ source: { id: sourceID }, target: { id: targetID }});
			relations.push(onerelation);
		}
		//组合关系Composition（实线+黑菱形）
		if(links[i].relationship==='Composition'){
			var onerelation=new uml.Composition({ source: { id: sourceID }, target: { id: targetID }});
			relations.push(onerelation);
		}
		//使用关系Dependency（实线+黑箭头）
		if(links[i].relationship==='Transition'){
			var onerelation=new uml.Transition({ source: { id: sourceID }, target: { id: targetID }});
			relations.push(onerelation);
		}
	}
	return relations;
}
/**   
 * Simple Map   
 *     
 *    
 * @author JQH   
 * @date 2019-06-2  
 */    
function Map() {     
    /** 存放键的数组(遍历用到) */    
    this.keys = new Array();     
    /** 存放数据 */    
    this.data = new Object();     

    /**   
     * 放入一个键值对   
     * @param {String} key   
     * @param {Object} value   
     */    
    this.put = function(key, value) {     
        if(this.data[key] == null){     
            this.keys.push(key);     
        }     
        this.data[key] = value;     
    };     

    /**   
     * 获取某键对应的值   
     * @param {String} key   
     * @return {Object} value   
     */    
    this.get = function(key) {     
        return this.data[key];     
    };     

    /**   
     * 删除一个键值对   
     * @param {String} key   
     */    
    this.remove = function(key) {     
        this.keys.remove(key);     
        this.data[key] = null;     
    };     

    /**   
     * 遍历Map,执行处理函数   
     *    
     * @param {Function} 回调函数 function(key,value,index){..}   
     */    
    this.each = function(fn){     
        if(typeof fn != 'function'){     
            return;     
        }     
        var len = this.keys.length;     
        for(var i=0;i<len;i++){     
            var k = this.keys[i];     
            fn(k,this.data[k],i);     
        }     
    };     

    /**   
     * 获取键值数组(类似<a href="http://lib.csdn.net/base/java" class='replace_word' title="Java 知识库" target='_blank' style='color:#df3434; font-weight:bold;'>Java</a>的entrySet())   
     * @return 键值对象{key,value}的数组   
     */    
    this.entrys = function() {     
        var len = this.keys.length;     
        var entrys = new Array(len);     
        for (var i = 0; i < len; i++) {     
            entrys[i] = {     
                key : this.keys[i],     
                value : this.data[i]     
            };     
        }     
        return entrys;     
    };     

    /**   
     * 判断Map是否为空   
     */    
    this.isEmpty = function() {     
        return this.keys.length == 0;     
    };     

    /**   
     * 获取键值对数量   
     */    
    this.size = function(){     
        return this.keys.length;     
    };     

    /**   
     * 重写toString    
     */    
    this.toString = function(){     
        var s = "{";     
        for(var i=0;i<this.keys.length;i++,s+=','){     
            var k = this.keys[i];     
            s += k+"="+this.data[k];     
        }     
        s+="}";     
        return s;     
    };     
}     