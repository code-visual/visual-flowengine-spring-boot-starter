import{r as u,W as T}from"./index-RH1sRjLc.js";function be(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function ne(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter(function(i){return Object.getOwnPropertyDescriptor(e,i).enumerable})),r.push.apply(r,n)}return r}function ie(e){for(var t=1;t<arguments.length;t++){var r=arguments[t]!=null?arguments[t]:{};t%2?ne(Object(r),!0).forEach(function(n){be(e,n,r[n])}):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):ne(Object(r)).forEach(function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(r,n))})}return e}function we(e,t){if(e==null)return{};var r={},n=Object.keys(e),i,o;for(o=0;o<n.length;o++)i=n[o],!(t.indexOf(i)>=0)&&(r[i]=e[i]);return r}function ye(e,t){if(e==null)return{};var r=we(e,t),n,i;if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(i=0;i<o.length;i++)n=o[i],!(t.indexOf(n)>=0)&&Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}function Oe(e,t){return je(e)||Me(e,t)||Se(e,t)||Ee()}function je(e){if(Array.isArray(e))return e}function Me(e,t){if(!(typeof Symbol>"u"||!(Symbol.iterator in Object(e)))){var r=[],n=!0,i=!1,o=void 0;try{for(var s=e[Symbol.iterator](),p;!(n=(p=s.next()).done)&&(r.push(p.value),!(t&&r.length===t));n=!0);}catch(h){i=!0,o=h}finally{try{!n&&s.return!=null&&s.return()}finally{if(i)throw o}}return r}}function Se(e,t){if(e){if(typeof e=="string")return oe(e,t);var r=Object.prototype.toString.call(e).slice(8,-1);if(r==="Object"&&e.constructor&&(r=e.constructor.name),r==="Map"||r==="Set")return Array.from(e);if(r==="Arguments"||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(r))return oe(e,t)}}function oe(e,t){(t==null||t>e.length)&&(t=e.length);for(var r=0,n=new Array(t);r<t;r++)n[r]=e[r];return n}function Ee(){throw new TypeError(`Invalid attempt to destructure non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`)}function Pe(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function ae(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter(function(i){return Object.getOwnPropertyDescriptor(e,i).enumerable})),r.push.apply(r,n)}return r}function ue(e){for(var t=1;t<arguments.length;t++){var r=arguments[t]!=null?arguments[t]:{};t%2?ae(Object(r),!0).forEach(function(n){Pe(e,n,r[n])}):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):ae(Object(r)).forEach(function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(r,n))})}return e}function Ie(){for(var e=arguments.length,t=new Array(e),r=0;r<e;r++)t[r]=arguments[r];return function(n){return t.reduceRight(function(i,o){return o(i)},n)}}function z(e){return function t(){for(var r=this,n=arguments.length,i=new Array(n),o=0;o<n;o++)i[o]=arguments[o];return i.length>=e.length?e.apply(this,i):function(){for(var s=arguments.length,p=new Array(s),h=0;h<s;h++)p[h]=arguments[h];return t.apply(r,[].concat(i,p))}}}function K(e){return{}.toString.call(e).includes("Object")}function Re(e){return!Object.keys(e).length}function N(e){return typeof e=="function"}function Ce(e,t){return Object.prototype.hasOwnProperty.call(e,t)}function Ae(e,t){return K(t)||E("changeType"),Object.keys(t).some(function(r){return!Ce(e,r)})&&E("changeField"),t}function Le(e){N(e)||E("selectorType")}function Te(e){N(e)||K(e)||E("handlerType"),K(e)&&Object.values(e).some(function(t){return!N(t)})&&E("handlersType")}function $e(e){e||E("initialIsRequired"),K(e)||E("initialType"),Re(e)&&E("initialContent")}function De(e,t){throw new Error(e[t]||e.default)}var Ve={initialIsRequired:"initial state is required",initialType:"initial state should be an object",initialContent:"initial state shouldn't be an empty object",handlerType:"handler should be an object or a function",handlersType:"all handlers should be a functions",selectorType:"selector should be a function",changeType:"provided value of changes should be an object",changeField:'it seams you want to change a field in the state which is not specified in the "initial" state',default:"an unknown error accured in `state-local` package"},E=z(De)(Ve),B={changes:Ae,selector:Le,handler:Te,initial:$e};function xe(e){var t=arguments.length>1&&arguments[1]!==void 0?arguments[1]:{};B.initial(e),B.handler(t);var r={current:e},n=z(Fe)(r,t),i=z(ze)(r),o=z(B.changes)(e),s=z(qe)(r);function p(){var w=arguments.length>0&&arguments[0]!==void 0?arguments[0]:function(I){return I};return B.selector(w),w(r.current)}function h(w){Ie(n,i,o,s)(w)}return[p,h]}function qe(e,t){return N(t)?t(e.current):t}function ze(e,t){return e.current=ue(ue({},e.current),t),t}function Fe(e,t,r){return N(t)?t(e.current):Object.keys(r).forEach(function(n){var i;return(i=t[n])===null||i===void 0?void 0:i.call(t,e.current[n])}),r}var Ne={create:xe},Ue={paths:{vs:"https://cdn.jsdelivr.net/npm/monaco-editor@0.43.0/min/vs"}};function We(e){return function t(){for(var r=this,n=arguments.length,i=new Array(n),o=0;o<n;o++)i[o]=arguments[o];return i.length>=e.length?e.apply(this,i):function(){for(var s=arguments.length,p=new Array(s),h=0;h<s;h++)p[h]=arguments[h];return t.apply(r,[].concat(i,p))}}}function He(e){return{}.toString.call(e).includes("Object")}function Be(e){return e||ce("configIsRequired"),He(e)||ce("configType"),e.urls?(Ge(),{paths:{vs:e.urls.monacoBase}}):e}function Ge(){console.warn(le.deprecation)}function Ke(e,t){throw new Error(e[t]||e.default)}var le={configIsRequired:"the configuration object is required",configType:"the configuration object should be an object",default:"an unknown error accured in `@monaco-editor/loader` package",deprecation:`Deprecation warning!
    You are using deprecated way of configuration.

    Instead of using
      monaco.config({ urls: { monacoBase: '...' } })
    use
      monaco.config({ paths: { vs: '...' } })

    For more please check the link https://github.com/suren-atoyan/monaco-loader#config
  `},ce=We(Ke)(le),Ye={config:Be},ke=function(){for(var t=arguments.length,r=new Array(t),n=0;n<t;n++)r[n]=arguments[n];return function(i){return r.reduceRight(function(o,s){return s(o)},i)}};function se(e,t){return Object.keys(t).forEach(function(r){t[r]instanceof Object&&e[r]&&Object.assign(t[r],se(e[r],t[r]))}),ie(ie({},e),t)}var Je={type:"cancelation",msg:"operation is manually canceled"};function ee(e){var t=!1,r=new Promise(function(n,i){e.then(function(o){return t?i(Je):n(o)}),e.catch(i)});return r.cancel=function(){return t=!0},r}var Qe=Ne.create({config:Ue,isInitialized:!1,resolve:null,reject:null,monaco:null}),fe=Oe(Qe,2),U=fe[0],Y=fe[1];function Xe(e){var t=Ye.config(e),r=t.monaco,n=ye(t,["monaco"]);Y(function(i){return{config:se(i.config,n),monaco:r}})}function Ze(){var e=U(function(t){var r=t.monaco,n=t.isInitialized,i=t.resolve;return{monaco:r,isInitialized:n,resolve:i}});if(!e.isInitialized){if(Y({isInitialized:!0}),e.monaco)return e.resolve(e.monaco),ee(te);if(window.monaco&&window.monaco.editor)return de(window.monaco),e.resolve(window.monaco),ee(te);ke(_e,tt)(rt)}return ee(te)}function _e(e){return document.body.appendChild(e)}function et(e){var t=document.createElement("script");return e&&(t.src=e),t}function tt(e){var t=U(function(n){var i=n.config,o=n.reject;return{config:i,reject:o}}),r=et("".concat(t.config.paths.vs,"/loader.js"));return r.onload=function(){return e()},r.onerror=t.reject,r}function rt(){var e=U(function(r){var n=r.config,i=r.resolve,o=r.reject;return{config:n,resolve:i,reject:o}}),t=window.require;t.config(e.config),t(["vs/editor/editor.main"],function(r){de(r),e.resolve(r)},function(r){e.reject(r)})}function de(e){U().monaco||Y({monaco:e})}function nt(){return U(function(e){var t=e.monaco;return t})}var te=new Promise(function(e,t){return Y({resolve:e,reject:t})}),pe={config:Xe,init:Ze,__getMonacoInstance:nt},it={wrapper:{display:"flex",position:"relative",textAlign:"initial"},fullWidth:{width:"100%"},hide:{display:"none"}},re=it,ot={container:{display:"flex",height:"100%",width:"100%",justifyContent:"center",alignItems:"center"}},at=ot;function ut({children:e}){return T.createElement("div",{style:at.container},e)}var ct=ut,lt=ct;function st({width:e,height:t,isEditorReady:r,loading:n,_ref:i,className:o,wrapperProps:s}){return T.createElement("section",{style:{...re.wrapper,width:e,height:t},...s},!r&&T.createElement(lt,null,n),T.createElement("div",{ref:i,style:{...re.fullWidth,...!r&&re.hide},className:o}))}var ft=st,ge=u.memo(ft);function dt(e){u.useEffect(e,[])}var he=dt;function pt(e,t,r=!0){let n=u.useRef(!0);u.useEffect(n.current||!r?()=>{n.current=!1}:e,t)}var j=pt;function F(){}function L(e,t,r,n){return gt(e,n)||ht(e,t,r,n)}function gt(e,t){return e.editor.getModel(ve(e,t))}function ht(e,t,r,n){return e.editor.createModel(t,r,n?ve(e,n):void 0)}function ve(e,t){return e.Uri.parse(t)}function vt({original:e,modified:t,language:r,originalLanguage:n,modifiedLanguage:i,originalModelPath:o,modifiedModelPath:s,keepCurrentOriginalModel:p=!1,keepCurrentModifiedModel:h=!1,theme:w="light",loading:I="Loading...",options:M={},height:k="100%",width:J="100%",className:Q,wrapperProps:X={},beforeMount:Z=F,onMount:_=F}){let[y,$]=u.useState(!1),[P,v]=u.useState(!0),m=u.useRef(null),g=u.useRef(null),D=u.useRef(null),b=u.useRef(_),l=u.useRef(Z),R=u.useRef(!1);he(()=>{let a=pe.init();return a.then(f=>(g.current=f)&&v(!1)).catch(f=>(f==null?void 0:f.type)!=="cancelation"&&console.error("Monaco initialization: error:",f)),()=>m.current?V():a.cancel()}),j(()=>{if(m.current&&g.current){let a=m.current.getOriginalEditor(),f=L(g.current,e||"",n||r||"text",o||"");f!==a.getModel()&&a.setModel(f)}},[o],y),j(()=>{if(m.current&&g.current){let a=m.current.getModifiedEditor(),f=L(g.current,t||"",i||r||"text",s||"");f!==a.getModel()&&a.setModel(f)}},[s],y),j(()=>{let a=m.current.getModifiedEditor();a.getOption(g.current.editor.EditorOption.readOnly)?a.setValue(t||""):t!==a.getValue()&&(a.executeEdits("",[{range:a.getModel().getFullModelRange(),text:t||"",forceMoveMarkers:!0}]),a.pushUndoStop())},[t],y),j(()=>{var a,f;(f=(a=m.current)==null?void 0:a.getModel())==null||f.original.setValue(e||"")},[e],y),j(()=>{let{original:a,modified:f}=m.current.getModel();g.current.editor.setModelLanguage(a,n||r||"text"),g.current.editor.setModelLanguage(f,i||r||"text")},[r,n,i],y),j(()=>{var a;(a=g.current)==null||a.editor.setTheme(w)},[w],y),j(()=>{var a;(a=m.current)==null||a.updateOptions(M)},[M],y);let W=u.useCallback(()=>{var S;if(!g.current)return;l.current(g.current);let a=L(g.current,e||"",n||r||"text",o||""),f=L(g.current,t||"",i||r||"text",s||"");(S=m.current)==null||S.setModel({original:a,modified:f})},[r,t,i,e,n,o,s]),H=u.useCallback(()=>{var a;!R.current&&D.current&&(m.current=g.current.editor.createDiffEditor(D.current,{automaticLayout:!0,...M}),W(),(a=g.current)==null||a.editor.setTheme(w),$(!0),R.current=!0)},[M,w,W]);u.useEffect(()=>{y&&b.current(m.current,g.current)},[y]),u.useEffect(()=>{!P&&!y&&H()},[P,y,H]);function V(){var f,S,C,x;let a=(f=m.current)==null?void 0:f.getModel();p||((S=a==null?void 0:a.original)==null||S.dispose()),h||((C=a==null?void 0:a.modified)==null||C.dispose()),(x=m.current)==null||x.dispose()}return T.createElement(ge,{width:J,height:k,isEditorReady:y,loading:I,_ref:D,className:Q,wrapperProps:X})}var mt=vt;u.memo(mt);function bt(e){let t=u.useRef();return u.useEffect(()=>{t.current=e},[e]),t.current}var wt=bt,G=new Map;function yt({defaultValue:e,defaultLanguage:t,defaultPath:r,value:n,language:i,path:o,theme:s="light",line:p,loading:h="Loading...",options:w={},overrideServices:I={},saveViewState:M=!0,keepCurrentModel:k=!1,width:J="100%",height:Q="100%",className:X,wrapperProps:Z={},beforeMount:_=F,onMount:y=F,onChange:$,onValidate:P=F}){let[v,m]=u.useState(!1),[g,D]=u.useState(!0),b=u.useRef(null),l=u.useRef(null),R=u.useRef(null),W=u.useRef(y),H=u.useRef(_),V=u.useRef(),a=u.useRef(n),f=wt(o),S=u.useRef(!1),C=u.useRef(!1);he(()=>{let c=pe.init();return c.then(d=>(b.current=d)&&D(!1)).catch(d=>(d==null?void 0:d.type)!=="cancelation"&&console.error("Monaco initialization: error:",d)),()=>l.current?me():c.cancel()}),j(()=>{var d,O,q,A;let c=L(b.current,e||n||"",t||i||"",o||r||"");c!==((d=l.current)==null?void 0:d.getModel())&&(M&&G.set(f,(O=l.current)==null?void 0:O.saveViewState()),(q=l.current)==null||q.setModel(c),M&&((A=l.current)==null||A.restoreViewState(G.get(o))))},[o],v),j(()=>{var c;(c=l.current)==null||c.updateOptions(w)},[w],v),j(()=>{!l.current||n===void 0||(l.current.getOption(b.current.editor.EditorOption.readOnly)?l.current.setValue(n):n!==l.current.getValue()&&(C.current=!0,l.current.executeEdits("",[{range:l.current.getModel().getFullModelRange(),text:n,forceMoveMarkers:!0}]),l.current.pushUndoStop(),C.current=!1))},[n],v),j(()=>{var d,O;let c=(d=l.current)==null?void 0:d.getModel();c&&i&&((O=b.current)==null||O.editor.setModelLanguage(c,i))},[i],v),j(()=>{var c;p!==void 0&&((c=l.current)==null||c.revealLine(p))},[p],v),j(()=>{var c;(c=b.current)==null||c.editor.setTheme(s)},[s],v);let x=u.useCallback(()=>{var c;if(!(!R.current||!b.current)&&!S.current){H.current(b.current);let d=o||r,O=L(b.current,n||e||"",t||i||"",d||"");l.current=(c=b.current)==null?void 0:c.editor.create(R.current,{model:O,automaticLayout:!0,...w},I),M&&l.current.restoreViewState(G.get(d)),b.current.editor.setTheme(s),p!==void 0&&l.current.revealLine(p),m(!0),S.current=!0}},[e,t,r,n,i,o,w,I,M,s,p]);u.useEffect(()=>{v&&W.current(l.current,b.current)},[v]),u.useEffect(()=>{!g&&!v&&x()},[g,v,x]),a.current=n,u.useEffect(()=>{var c,d;v&&$&&((c=V.current)==null||c.dispose(),V.current=(d=l.current)==null?void 0:d.onDidChangeModelContent(O=>{C.current||$(l.current.getValue(),O)}))},[v,$]),u.useEffect(()=>{if(v){let c=b.current.editor.onDidChangeMarkers(d=>{var q;let O=(q=l.current.getModel())==null?void 0:q.uri;if(O&&d.find(A=>A.path===O.path)){let A=b.current.editor.getModelMarkers({resource:O});P==null||P(A)}});return()=>{c==null||c.dispose()}}return()=>{}},[v,P]);function me(){var c,d;(c=V.current)==null||c.dispose(),k?M&&G.set(o,l.current.saveViewState()):(d=l.current.getModel())==null||d.dispose(),l.current.dispose()}return T.createElement(ge,{width:J,height:Q,isEditorReady:v,loading:h,_ref:R,className:X,wrapperProps:Z})}var Ot=yt,jt=u.memo(Ot),St=jt;export{St as F,jt as d};
