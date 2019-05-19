/**
 * Copyright &copy; 2015-2020 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.jeeplus.modules.contract.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jeeplus.common.config.Global;
import com.jeeplus.common.json.AjaxJson;
import com.jeeplus.common.utils.DateUtils;
import com.jeeplus.common.utils.StringUtils;
import com.jeeplus.common.utils.excel.ExportExcel;
import com.jeeplus.common.utils.excel.ImportExcel;
import com.jeeplus.core.persistence.Page;
import com.jeeplus.core.web.BaseController;
import com.jeeplus.modules.act.entity.Act;
import com.jeeplus.modules.act.service.ActProcessService;
import com.jeeplus.modules.act.service.ActTaskService;
import com.jeeplus.modules.contract.entity.*;
import com.jeeplus.modules.contract.service.*;
import com.jeeplus.modules.supplier.entity.TSupplier;
import com.jeeplus.modules.supplier.utils.Word2Pdf;
import com.jeeplus.modules.sys.utils.UserUtils;
import com.jeeplus.modules.tsupmanager.service.TSupplierManagerService;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 合同管理Controller
 * @author 李春来
 * @version 2019-03-20
 */
@Component
@Controller
@RequestMapping(value = "${adminPath}/contract/tContract")
public class TContractController extends BaseController {
	private String aaa = "aaaaaaaaaaaaaaaaaaaaaaaaaa";
	@Autowired
	private TContractService tContractService;
	@Autowired
	private TSupplierManagerService tSupplierService;
	@Autowired
	private TContracttemplateService tContracttemplateService;
	@Autowired
	private TContracttemplateitemService tContracttemplateitemService;
	@Autowired
	private TContractdetailsService tContractdetailsService;
	@Autowired
	private TContractpayplanService tContractpayplanService;
	@Autowired
	private TContractitemService tContractitemService;
	@Autowired
	private TContractattachService tContractattachService;
	@Autowired
	private ActProcessService actProcessService;
	@Autowired
	private ActTaskService actTaskService;

	private String contractNo = "";
	private String contractType = "";

	private List<TContractDetails> tContractDetailsList = new ArrayList<TContractDetails>();
	private List<TContractPayPlan> tContractPayPlanList = new ArrayList<TContractPayPlan>();
	private List<TContractItem> tContractItemList = new ArrayList<TContractItem>();
	private List<TContractAttach> tContractAttachList = new ArrayList<TContractAttach>();

	@ModelAttribute
	public TContract get(@RequestParam(required=false) String id) {
		TContract entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = tContractService.get(id);
		}
		if (entity == null){
			entity = new TContract();
		}
		return entity;
	}

	/**
	 * 合同数据管理列表页面（李春来）
	 * @param tContract
	 * @param model
	 * @return
	 */
	@RequiresPermissions("contract:tContract:contractDataList")
	@RequestMapping(value = {"contractDataList", ""})
	public String contractDataList(TContract tContract, Model model){
		model.addAttribute("tContract", tContract);
        return "modules/contract/ContractDataList";
	}

	@RequestMapping(value = "gettContractItemList")
	@ResponseBody
	public List<TContractItem> gettContractItemList(String contractNo){
		List<TContractItem> list = tContractitemService.findItemByNo(contractNo);
		return list;
	}

	@RequestMapping(value = "getApproveInfoPage")
	public String getApproveInfoPage(){
		return "modules/contract/ApproveList";
	}

	@RequestMapping(value = "getApproveInfoList",method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getApproveInfoList(Approve approve, HttpServletRequest request, HttpServletResponse response, Model model){
		Page<Approve> page = tContractService.getApproveInfoList(new Page<Approve>(request, response), approve);
		return getBootstrapData(page);
	}

	@RequestMapping(value = "getApproveInfo")
	@ResponseBody
	public Map<String,Object> getApproveInfo(ApproveList approveList, HttpServletRequest request, HttpServletResponse response, Model model){
		Page<ApproveList> page = tContractService.getApproveInfo(new Page<ApproveList>(request, response), approveList);
		return getBootstrapData(page);
	}

	@RequestMapping(value = "{editform}")
	public String editform(Approve approve, Model model) {
		model.addAttribute("approve", approve);
		return "modules/contract/ApproveViewForm";
	}

	/**
	 * 合同数据管理列表数据（李春来）
	 * @param tContractVo
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequiresPermissions("contract:tContract:contractDataList")
	@RequestMapping(value = "contractData")
	public Map<String, Object> contractData(TContractVo tContractVo, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<TContractVo> page = tContractService.findContratDataListPage(new Page<TContractVo>(request, response), tContractVo);
		return getBootstrapData(page);
	}

	/**
	 * 合同审批管理列表页面
	 * @param tContract
	 * @param model
	 * @return
	 */
	@RequiresPermissions("contract:tContract:contractApproveList")
	@RequestMapping(value = {"contractApproveList", ""})
	public String contractApproveList(TContract tContract, Model model){
		model.addAttribute("tContract", tContract);
		return "modules/contract/ContractApproveList";
	}

	@ResponseBody
	@RequiresPermissions("contract:tContract:contractApproveList")
	@RequestMapping(value = "contractApproveData")
	public Map<String, Object> contractApproveData(TContractVo tContractVo, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<TContractVo> page = tContractService.findContractApproveDataListPage(new Page<TContractVo>(request, response), tContractVo);
		return getBootstrapData(page);
	}

	@ResponseBody
	@RequiresPermissions(value={"contract:tContract:agree","contract:tContract:reject"},logical=Logical.OR)
	@RequestMapping(value = "setContractStatus")
	public AjaxJson setContractStatus(String ids,String status) throws Exception{
		AjaxJson j = new AjaxJson();
		String idArray[] =ids.split(",");
		for(String no : idArray){
			TContract tContract = tContractService.findContractByNo(no);
			tContract.setStatus(status);
			tContract.setIsNewRecord(false);
			tContractService.save(tContract);
			if(status.equals("2")){
				createContract(tContract);
			}
		}
		j.setSuccess(true);
		if(status.equals("2")){
			j.setMsg("同意合同审批");
		}else if(status.equals("3")){
			j.setMsg("驳回合同审批");
		}
		return j;
	}

	@ResponseBody
	@RequestMapping(value = "submitApproveContract")
	public AjaxJson submitApproveContract(String id,Act act) throws Exception{
		AjaxJson j = new AjaxJson();
		TContract tContract = tContractService.get(id);
		if(act!=null){
			tContract.setAct(act);
		}
		tContract.setIsNewRecord(false);
		if ("0".equals(tContract.getStatus())){
			tContract.setStatus("1");
			//新增或编辑表单保存
			j = tContractService.saveContract(tContract);//保存||"3".equals(tContract.getStatus())
			// 启动流程
			ProcessDefinition p = actProcessService.getProcessDefinitionByName("合同审批");
			String title = UserUtils.getUser().getName()+"在"+ DateUtils.getDateTime()+"发起"+p.getName();
			String pid = p.getId();
			String pkey = p.getKey();
			actTaskService.startProcess(p.getKey(),  "t_contract", tContract.getId(), title);
			//j.setMsg("发起流程审批成功!");
			j.setSuccess(true);
			j.setMsg(j.getMsg()+"提交合同成功");
			j.getBody().put("targetUrl",  "/contract/tContract");
		}else if("1".equals(tContract.getStatus()) && tContract.getAct()!=null && StringUtils.isNotBlank(tContract.getAct().getFlag())){
			//新增或编辑表单保存
			j = tContractService.saveContract(tContract);
			tContract.getAct().setComment(("yes".equals(tContract.getAct().getFlag())?"[重新申请] ":"[销毁申请] "));
			// 完成流程任务
			Map<String, Object> vars = Maps.newHashMap();
			vars.put("reapply", "yes".equals(tContract.getAct().getFlag())? true : false);
			actTaskService.complete(tContract.getAct().getTaskId(), tContract.getAct().getProcInsId(), tContract.getAct().getComment(), tContract.getContent(), vars);
			//j.setMsg("提交成功！");
			j.setSuccess(true);
			j.setMsg(j.getMsg()+"提交合同成功");
			j.getBody().put("targetUrl",  "/act/task/todo/");
		}else{
			tContract.setStatus("1");
			j = tContractService.saveContract(tContract);//保存
			j.setMsg(j.getMsg()+"提交合同成功");
			j.getBody().put("targetUrl",  "/contract/tContract");
		}
		return j;
	}

	@RequestMapping(value = "createContractDataForm")
	public String createContractDataForm(TContract tContract, Model model){
		tContractDetailsList.clear();
		tContractPayPlanList.clear();
		tContractAttachList.clear();
		tContractItemList.clear();

		contractNo = tContractService.queryContractNo(tContract);
		contractType = "0";
		List<TContracttemplate> contractTemplateList = tContracttemplateService.findAllTemplateList();
		model.addAttribute("contractTemplateList", contractTemplateList);
		model.addAttribute("tContract",new TContract());
        model.addAttribute("mode", "add");
		return "modules/contract/ContractDataForm";
	}

	@ResponseBody
	@RequestMapping(value = "getTemplateItemData")
	public AjaxJson getTemplateItemData(HttpServletRequest request,Model model){
		AjaxJson j = new AjaxJson();
		String templateKey = request.getParameter("templatekey");
		TContracttemplateitem tContracttemplateitem = new TContracttemplateitem();
		tContracttemplateitem.setTemplatekey(templateKey);
		List<TContracttemplateitem> tContracttemplateitemList = tContracttemplateitemService.findAllContractTemplateItemList(tContracttemplateitem);
		List<TContracttemplate> tContracttemplateList = tContracttemplateService.findAllTemplateList();

		for(TContracttemplateitem tContracttemplateitem1 : tContracttemplateitemList){
			TContractItem tContractItem = new TContractItem();
			tContractItem.setClauseno(tContracttemplateitem1.getItemkey());
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname(tContracttemplateitem1.getItemname());
			tContractItem.setItemvalue(tContracttemplateitem1.getItemcontent());
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractItemList.add(tContractItem);
		}
		model.addAttribute("tContracttemplateitemList", tContracttemplateitemList);
		model.addAttribute("tContracttemplateList", tContracttemplateList);
		j.setSuccess(true);
		j.setMsg("获得合同模板条款成功");
		return j;
	}

	@RequestMapping(value = "auditContractDataForm")
	public String auditContractDataForm(TContract tContract2,String id , Model model){
		tContractDetailsList.clear();
		tContractPayPlanList.clear();
		tContractAttachList.clear();
		tContractItemList.clear();

		TContract tContract = tContractService.get(id);
		contractNo = tContract.getContractno();
		tContractDetailsList = tContractService.findContractDetailByNo(contractNo);
		tContractPayPlanList = tContractService.findContractPayplanByNo(contractNo);
		tContractAttachList = tContractService.findContractAttachByNo(contractNo);
		tContractItemList = tContractService.findContractItemByNo(contractNo);
		boolean isSystemTemplate = false;
		if(tContractItemList.size()==0){
			isSystemTemplate = true;
		}

		String mode = "";

		List<TContracttemplate> contractTemplateList = tContracttemplateService.findAllTemplateList();
		model.addAttribute("contractTemplateList", contractTemplateList);
		model.addAttribute("tContract", tContract);
		model.addAttribute("isSystemTemplate", isSystemTemplate);
		model.addAttribute("mode", mode);
		return "modules/contract/ContractDataForm";
	}

	@RequestMapping(value = "editContractDataForm")
	public String editContractDataForm(TContract tContract2,String id , Model model){
		Act act = tContract2.getAct();

		tContractDetailsList.clear();
		tContractPayPlanList.clear();
		tContractAttachList.clear();
		tContractItemList.clear();

		TContract tContract = tContractService.get(id);
		contractNo = tContract.getContractno();
		tContractDetailsList = tContractService.findContractDetailByNo(contractNo);
		tContractPayPlanList = tContractService.findContractPayplanByNo(contractNo);
		tContractAttachList = tContractService.findContractAttachByNo(contractNo);
		tContractItemList = tContractService.findContractItemByNo(contractNo);
		boolean isSystemTemplate = false;
		if(tContractItemList.size()==0){
			isSystemTemplate = true;
		}

		String mode = "";
		if(!tContract.getStatus().equals("2")){
			if(act!=null){
				tContract.setStatus("3");
				tContract.setAct(act);
				mode = "audit";
				contractType = "2";
			}else{
				mode = "edit";
				contractType = "1";
			}
		} else{
			mode = "";
			contractType = "1";
		}

		List<TContracttemplate> contractTemplateList = tContracttemplateService.findAllTemplateList();
		model.addAttribute("contractTemplateList", contractTemplateList);
		model.addAttribute("tContract", tContract);
		model.addAttribute("act", act);
		model.addAttribute("isSystemTemplate", isSystemTemplate);
		model.addAttribute("mode", mode);
		return "modules/contract/ContractDataForm";
	}

	@ResponseBody
	@RequestMapping(value = "getContractItemListByNo")
	public List<TContractItem> getContractItemListByNo(){
		return tContractItemList;
	}

	@RequestMapping(value = "previewContract")
	public void previewContract(HttpServletRequest request,HttpServletResponse response){
		String id = request.getParameter("id");
		TContract tContract = tContractService.findUniqueByProperty("contractno",id);
		if(tContract.getStatus().equals("2")){
			String filename = tContract.getFilename();
			String targetPath = tContract.getFilepath();
			String change = Word2Pdf.doc2pdf(targetPath);
			String path=change;
			response.setContentType("application/pdf;charset=UTF-8");
			response.setHeader("Content-Disposition",
					"inline; filename="+filename.substring(0,filename.length()-4));
			OutputStream out = null;
			try {
				out =response.getOutputStream();
				InputStream in = new FileInputStream(path);
				int len=0;
				byte [] buffer = new byte[1024];
				while((len=in.read(buffer))>0)
					out.write(buffer, 0, len);
				out.flush();
				in.close();
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createContract(TContract tContract) throws Exception{
		if(tContract.getStatus().equals("2")){
			String contractno = tContract.getContractno();
			String supno = tContract.getSupno();
			TSupplier tSupplier = tSupplierService.findUniqueByProperty("supno",supno);
			String suppliername = tSupplier.getSupnamecn();
			List<TContractDetails> tContractDetailsList = tContractService.findContractDetailByNo(contractno);
			String totalmonkey = tContract.getTotalmonkey()+"";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			List<TContractPayPlan> tContractPayPlanList = tContractService.findContractPayplanByNo(contractno);
			String payPlan = "";
			if(tContractPayPlanList.size()==1){
				TContractPayPlan tContractPayPlan = tContractPayPlanList.get(0);
				String paymentDate = tContractPayPlan.getPaymentdate();

				long days = (sdf.parse(sdf.format(paymentDate)).getTime()-sdf.parse(sdf.format(new Date())).getTime())/(24*60*60*1000);
				payPlan = "一次性付款：款到发货，经买方验收合格之日起"+days+"日内，卖方要向买方提供本合同的全额增值税专用发票。";
			}
			else{
				payPlan = "分期付款：本合同签订后";
				for(TContractPayPlan tContractPayPlan : tContractPayPlanList){
					payPlan += "于"+tContractPayPlan.getPaymentdate().substring(0,10)+"日支付合同总价的"
							+tContractPayPlan.getPaymentproportion()+"%,计"+tContractPayPlan.getPaymentmoney()+"元。";
				}
			}
			String openbank = tSupplier.getBank();
			String bank = tSupplier.getBankno();
			List<TContractItem> tContractItemList = tContractService.findContractItemByNo(contractno);
			String chairman = tSupplier.getChairman();
			String address = tSupplier.getAddress();
			String taxno = tSupplier.getTaxno();
			String phone = tSupplier.getPhone();
			String email = tSupplier.getEmail();
			String concludedate = tContract.getConcludeDate();

			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddhhmmss");
			String dateString = sdf2.format(new Date());
			String filename = "WDF-"+dateString+"采购合同.docx";

			String sourcePath= Global.getConfig("contractpath")+"\\source\\WDF.docx";
			String targetPath= Global.getConfig("contractpath")+"\\target\\"+filename;
			Map <String,String> map = new HashMap<String,String>();
			map.put("${p1}",contractno!=null?contractno:"");
			map.put("${p2}",suppliername!=null?suppliername:"");
			map.put("${p6}",payPlan!=null?payPlan:"");
			map.put("${p7}",openbank!=null?openbank:"");
			map.put("${p8}",bank!=null?bank:"");
			map.put("${p16}",chairman!=null?chairman:"");
			map.put("${p17}",address!=null?address:"");
			map.put("${p18}",taxno!=null?taxno:"");
			map.put("${p19}",phone!=null?phone:"");
			map.put("${p20}",email!=null?email:"");
			map.put("${p21}",concludedate!=null?concludedate.substring(0,10):"");
			for(TContractItem tContractItem:tContractItemList)
			{
				String itemName = tContractItem.getItemname();
				if(tContractItem.getItemname().indexOf("账户变更提前通知")!=-1){
					map.put("${p9}",tContractItem.getItemvalue()!=null?tContractItem.getItemvalue():"");
				}else if(tContractItem.getItemname().indexOf("交货提前通知天数")!=-1){
					map.put("${p10}",tContractItem.getItemvalue()!=null?tContractItem.getItemvalue():"");
				}else if(tContractItem.getItemname().indexOf("违约金比例")!=-1){
					map.put("${p11}",tContractItem.getItemvalue()!=null?tContractItem.getItemvalue():"");
				}else if(tContractItem.getItemname().indexOf("交付期限")!=-1){
					map.put("${p12}",tContractItem.getItemvalue()!=null?tContractItem.getItemvalue():"");
				}else if(tContractItem.getItemname().indexOf("质保期")!=-1){
					map.put("${p13}",tContractItem.getItemvalue()!=null?tContractItem.getItemvalue():"");
				}else if(tContractItem.getItemname().indexOf("违约金")!=-1){
					map.put("${p14}",tContractItem.getItemvalue()!=null?tContractItem.getItemvalue():"");
				}else if(tContractItem.getItemname().indexOf("有效期")!=-1){
					map.put("${p15}",tContractItem.getItemvalue()!=null?tContractItem.getItemvalue():"");
				}
			}
			Map <String,Object> map2 = new HashMap<String,Object>();
			List<Object> list = new ArrayList<Object>();
			list.add(tContractDetailsList);
			list.add(totalmonkey);
			map2.put("${p3}",list);
			tContractService.replaceWord(sourcePath,targetPath,map,map2);

			tContract.setFilename(filename);
			tContract.setFilepath(targetPath);
			tContract.setIsNewRecord(false);
			tContractService.save(tContract);
		}
	}

	@ResponseBody
	@RequestMapping(value = "download")
	public void download(String no, HttpServletResponse response) throws IOException {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		TContract tContract = tContractService.findUniqueByProperty("ContractNo",no);
		//获取下载文件露肩
		String downLoadPath = tContract.getFilepath();;
		String filename = tContract.getFilename();

		if(downLoadPath==null){
			response.getWriter().write("<script>alert('文件不存在');window.history.back(-1);</script>");
			return;
		}

		downLoadPath  = downLoadPath.substring(0,downLoadPath.length()-4)+"pdf";
		filename = filename.substring(0,filename.length()-4)+"pdf";
		//获取文件的长度
		long fileLength = new File(downLoadPath).length();

		if(fileLength ==0){
			response.getWriter().write("<script>alert('文件不存在');window.history.back(-1);</script>");
			return;
		}
		//设置文件输出类型
		response.setContentType("application/octet-stream");
		response.setHeader("Content-disposition", "attachment; filename="+new String(filename.getBytes("utf-8"), "ISO-8859-1"));
		//设置输出长度
		response.setHeader("Content-Length", String.valueOf(fileLength));
		//获取输入流
		bis = new BufferedInputStream(new FileInputStream(downLoadPath));
		//输出流
		bos = new BufferedOutputStream(response.getOutputStream());
		byte[] buff = new byte[2048];
		int bytesRead;
		while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
			bos.write(buff, 0, bytesRead);
		}
		//关闭流
		bis.close();
		bos.close();
	}


	@ResponseBody
	@RequestMapping(value = "createContractData")
	public AjaxJson createContractData(TContract tContract,HttpServletRequest request,Model model) throws Exception{
		AjaxJson j = new AjaxJson();
		String p1 = request.getParameter("p1");
		/**
		 * 后台hibernate-validation插件校验
		 */
		String errMsg = beanValidator(tContract);
		if (StringUtils.isNotBlank(errMsg)){
			j.setSuccess(false);
			j.setMsg(errMsg);
			return j;
		}
		String contractno = tContractService.queryContractNo(tContract);
		tContract.setContractno(contractno);
		tContract.setStatus("0");
		tContract.setIsNewRecord(true);
		//新增或编辑表单保存
		tContractService.save(tContract);//保存
		j.setSuccess(true);
		j.setMsg("保存合同管理成功");
		return j;
	}

	@ResponseBody
	@RequestMapping(value = "contractPayPlanData")
	public Map<String, Object> contractPayPlanData(TContractPayPlan tContractPayPlan, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<TContractPayPlan> page = tContractService.findContractPayPlanDataListPage(new Page<TContractPayPlan>(request, response), tContractPayPlan,tContractPayPlanList);
		return getBootstrapData(page);
	}

	@ResponseBody
	@RequestMapping(value = "editPlanDate")
	public AjaxJson editPlanDate(String paymentdate,String id,String paymentno){
		AjaxJson j = new AjaxJson();
		if(paymentdate.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写计划付款日期");
			return j;
		}

		for(TContractPayPlan tContractPayPlan : tContractPayPlanList){
			if(tContractPayPlan.getPaymentno().equals(paymentno)){
				tContractPayPlan.setPaymentdate(paymentdate);
			}
		}
		tContractService.editDate(paymentdate,id);
		j.setSuccess(true);
		j.setMsg("修改付款计划日期成功.");
		return j;
	}

	@ResponseBody
	@RequestMapping(value = "contractAttachData")
	public Map<String, Object> contractAttachData(TContractAttach tContractAttach, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<TContractAttach> page = tContractService.findContractAttachDataListPage(new Page<TContractAttach>(request, response), tContractAttach,tContractAttachList);
		return getBootstrapData(page);
	}

	@ResponseBody
	@RequestMapping(value = "importContractAttach")
	public AjaxJson importContractAttach(@RequestParam("file") MultipartFile file, HttpServletResponse response, HttpServletRequest request) throws Exception{
		AjaxJson j = new AjaxJson();
		String remark = new String(request.getParameter("fileremark").getBytes("ISO_8859-1"),"UTF-8");
		if(remark.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写文件说明");
			return j;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmsss");
		String filepath = sdf.format(new Date());
		Map<String, String> map = tContractService.importAttach(file, response, request, "/contract/"+filepath+"/attach");
		String fileName = map.get("fileName");
		String path = map.get("path");
		TContractAttach tContractAttach = new TContractAttach();
		String annexno = tContractService.queryContractAttachNo(tContractAttach);
		tContractAttach.setAnnexno(annexno);
		tContractAttach.setContractno(contractNo);
		tContractAttach.setFilename(fileName);
		tContractAttach.setPath(path);
		tContractAttach.setFileremark(remark);
		tContractAttach.setIsdeleted("0");
		tContractAttach.setIsNewRecord(true);
		tContractAttachList.add(tContractAttach);
		j.setSuccess(true);
		j.setMsg("保存合同附件成功");
		return j;
	}

	@ResponseBody
	@RequestMapping(value = "contractDetailData")
	public Map<String, Object> contractDetailData(TContractDetails tContractDetails, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<TContractDetails> page = tContractService.findContractDetailDataListPage(new Page<TContractDetails>(request, response), tContractDetails,tContractDetailsList);
		return getBootstrapData(page);
	}

	@ResponseBody
	@RequestMapping(value = "contractDetailDialogData")
	public Map<String, Object> contractDetailDialogData(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception{
		String materialno = new String(request.getParameter("materialno").getBytes("ISO-8859-1"),"UTF-8");
		String materialname = new String(request.getParameter("materialname").getBytes("ISO-8859-1"),"UTF-8");
		String chartype = new String(request.getParameter("chartype").getBytes("ISO-8859-1"),"UTF-8");
		String supno = request.getParameter("supno");
		TContractDetails tContractDetails = new TContractDetails();
		tContractDetails.setMaterialno(materialno);
		tContractDetails.setMaterialname(materialname);
		tContractDetails.setChartype(chartype);
		tContractDetails.setSupno(supno);
		Page<TContractDetails> page = tContractService.findApproveList(new Page<TContractDetails>(request, response),tContractDetails);
		return getBootstrapData(page);
	}

	/**
	 * 合同管理列表页面
	 */
	@RequiresPermissions("contract:tContract:list")
	@RequestMapping(value = {"list", ""})
	public String list(TContract tContract, Model model) {
		model.addAttribute("tContract", tContract);
		return "modules/contract/tContractList";
	}

		/**
	 * 合同管理列表数据
	 */
	@ResponseBody
	@RequiresPermissions("contract:tContract:list")
	@RequestMapping(value = "data")
	public Map<String, Object> data(TContract tContract, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<TContract> page = tContractService.findPage(new Page<TContract>(request, response), tContract);
		return getBootstrapData(page);
	}


	@RequestMapping(value = "formAudit")
	public String formAudit(TContract tContract, Model model) {
		Act act = tContract.getAct();
		if (StringUtils.isNotBlank(tContract.getId())){
			tContract = tContractService.get(tContract.getId());
			tContract.setAct(act);
		}
		model.addAttribute("tContract", tContract);
		model.addAttribute("mode","audit");
		return "modules/contract/tContractAudit";
	}

	@RequestMapping(value = "formAuditEdit")
	public String formAuditEdit(TContract tContract, Model model) {
		Act act = tContract.getAct();
		if (StringUtils.isNotBlank(tContract.getId())){
			tContract = tContractService.get(tContract.getId());
			tContract.setAct(act);
		}
		model.addAttribute("tContract", tContract);
		model.addAttribute("act",act);
		model.addAttribute("mode","audit");
		return "modules/contract/tContractAuditEdit";
	}

	/**
	 * 查看，增加，编辑合同管理表单页面
	 */
	@RequiresPermissions(value={"contract:tContract:view","contract:tContract:add","contract:tContract:edit"},logical=Logical.OR)
	@RequestMapping(value = "form/{mode}")
	public String form(@PathVariable String mode, TContract tContract, Model model) {
		model.addAttribute("tContract", tContract);
		model.addAttribute("mode", mode);
		return "modules/contract/ContractDataForm";
	}

	@RequestMapping(value = "formDetailsAdd")
	public String formDetailsAdd(TContract tContract, Model model) {
		model.addAttribute("tContract", tContract);
		model.addAttribute("mode","add");
		return "modules/contract/ContractDataDetails";
	}

	/**
	 * 保存合同管理
	 */
	@ResponseBody
	@RequiresPermissions(value={"contract:tContract:add","contract:tContract:edit"},logical=Logical.OR)
	@RequestMapping(value = "save")
	public AjaxJson save(HttpServletRequest request) throws Exception{
		AjaxJson j = new AjaxJson();
		String id = request.getParameter("id");
		boolean isSystemTemplate = Boolean.parseBoolean(request.getParameter("isSystemTemplate"));
		String contracttemplatekey = request.getParameter("contracttemplatekey");
		String contractname = request.getParameter("contractname");
		String concludeDate = request.getParameter("concludeDate");
		String supno = request.getParameter("supno");
		String supname = request.getParameter("supname");
		String purtype = request.getParameter("purtype");
		String businesstype = request.getParameter("businesstype");
		String contracttype = request.getParameter("contracttype");
		String department = request.getParameter("department");
		String proarrivedate = request.getParameter("proarrivedate");
		String taxrate = request.getParameter("taxrate");
		String currency = request.getParameter("currency");
		String exchangerate = request.getParameter("exchangerate");
		String paycondition = request.getParameter("paycondition");
		String persion = request.getParameter("persion");
		String taxtype = request.getParameter("taxtype");
		String comments = request.getParameter("comments");
		String item0 = request.getParameter("item0");
		String item1 = request.getParameter("item1");
		String item2 = request.getParameter("item2");
		String item3 = request.getParameter("item3");
		String item4 = request.getParameter("item4");
		String item5 = request.getParameter("item5");
		String item6 = request.getParameter("item6");
		String item7 = request.getParameter("item7");
		String taskId = request.getParameter("taskId");
		String taskDefKey = request.getParameter("taskDefKey");
		String procInsId = request.getParameter("procInsId");
		String procDefId = request.getParameter("procDefId");

		LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
		if(taskId!=null&&!taskId.equals("")){
			map.put("id",id);
			map.put("taskId",taskId);
			map.put("taskDefKey",taskDefKey);
			map.put("procInsId",procInsId);
			map.put("procDefId",procDefId);
		}

		if(!isSystemTemplate) {
			if(item0==null||item0.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写账户变更提前通知天数");
				return j;
			}

			if(item1==null||item1.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写卖方交货提前通知天数");
				return j;
			}

			if(item2==null||item2.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写买方验货不合格卖方按合同总价支付违约金比例");
				return j;
			}

			if(item3==null||item3.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写所有产品交付期限天数");
				return j;
			}

			if(item4==null||item4.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写保质期");
				return j;
			}

			if(item5==null||item5.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写不履行义务违约方向守约方按合同总价支付违约金比例");
				return j;
			}

			if(item6==null||item6.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写本合同有效起始日期");
				return j;
			}

			if(item7==null||item7.equals("")){
				j.setSuccess(false);
				j.setMsg("请填写本合同有效终止日期");
				return j;
			}

		}

		if(contractType.equals("0")){
			List<TContract> list = tContractService.findContractList();
			for(TContract tContract:list){
				if(tContract.getContractname().equals(contractname)){
					j.setSuccess(false);
					j.setMsg("该合同名已经存在!");
					return j;
				}
			}
		}

		if(tContractDetailsList.size()==0){
			j.setSuccess(false);
			j.setMsg("请添加合同标的");
			return j;
		}
		if(tContractPayPlanList.size()==0){
			j.setSuccess(false);
			j.setMsg("请添加付款计划");
			return j;
		}

		if(isSystemTemplate&&tContractAttachList.size()==0){
			j.setSuccess(false);
			j.setMsg("请添加附件");
			return j;
		}

		for(TContractDetails tContractDetails:tContractDetailsList){
			if(contractType.equals("0")){
				tContractDetails.setIsNewRecord(true);
			}else{
				tContractDetails.setIsNewRecord(false);
			}
			tContractService.updateApprove(tContractDetails.getApproveId());
			tContractdetailsService.save(tContractDetails);
		}

		for(TContractPayPlan tContractPayPlan:tContractPayPlanList){
			if(contractType.equals("0")){
				tContractPayPlan.setIsNewRecord(true);
			}else{
				tContractPayPlan.setIsNewRecord(false);
			}
			tContractpayplanService.save(tContractPayPlan);
		}

		for(TContractAttach tContractAttach : tContractAttachList){
			if(contractType.equals("0")){
				tContractAttach.setIsNewRecord(true);
			}else{
				tContractAttach.setIsNewRecord(false);
			}
			tContractattachService.save(tContractAttach);
		}

		if(item0!=null){
			TContractItem tContractItem = new TContractItem();
			String clauseno = tContractitemService.queryContractClauseNo(tContractItem);
			tContractItem.setClauseno(clauseno);
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname("账户变更提前通知");
			tContractItem.setItemvalue(item0);
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractitemService.save(tContractItem);
		}

		if(item1!=null){
			TContractItem tContractItem = new TContractItem();
			String clauseno = tContractitemService.queryContractClauseNo(tContractItem);
			tContractItem.setClauseno(clauseno);
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname("交货提前通知天数");
			tContractItem.setItemvalue(item1);
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractitemService.save(tContractItem);
		}

		if(item2!=null){
			TContractItem tContractItem = new TContractItem();
			String clauseno = tContractitemService.queryContractClauseNo(tContractItem);
			tContractItem.setClauseno(clauseno);
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname("违约金比例");
			tContractItem.setItemvalue(item2);
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractitemService.save(tContractItem);
		}

		if(item3!=null){
			TContractItem tContractItem = new TContractItem();
			String clauseno = tContractitemService.queryContractClauseNo(tContractItem);
			tContractItem.setClauseno(clauseno);
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname("交付期限");
			tContractItem.setItemvalue(item3);
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractitemService.save(tContractItem);
		}

		if(item4!=null){
			TContractItem tContractItem = new TContractItem();
			String clauseno = tContractitemService.queryContractClauseNo(tContractItem);
			tContractItem.setClauseno(clauseno);
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname("质保期");
			tContractItem.setItemvalue(item4);
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractitemService.save(tContractItem);
		}

		if(item5!=null){
			TContractItem tContractItem = new TContractItem();
			String clauseno = tContractitemService.queryContractClauseNo(tContractItem);
			tContractItem.setClauseno(clauseno);
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname("违约金");
			tContractItem.setItemvalue(item5);
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractitemService.save(tContractItem);
		}

		if(item6!=null&&item7!=null){
			TContractItem tContractItem = new TContractItem();
			String clauseno = tContractitemService.queryContractClauseNo(tContractItem);
			tContractItem.setClauseno(clauseno);
			tContractItem.setContractno(contractNo);
			tContractItem.setItemname("有效期");
			tContractItem.setItemvalue(item6+"至"+item7);
			tContractItem.setIsdeleted("0");
			tContractItem.setIsNewRecord(true);
			tContractitemService.save(tContractItem);
		}

		TContract tContract = null;
		if(contractType.equals("0")){
			tContract = new TContract();
			tContract.setContractno(contractNo);
			tContract.setContractname(contractname);
			tContract.setConcludeDate(concludeDate);
			tContract.setSupno(supno);
			tContract.setSupname(supname);
			tContract.setPurtype(purtype);
			tContract.setBusinesstype(businesstype);
			tContract.setContracttype(contracttype);
			tContract.setDepartment(department);
			tContract.setProarrivedate(proarrivedate);
			tContract.setTaxrate(taxrate);
			tContract.setCurrency(currency);
			tContract.setExchangerate(exchangerate);
			tContract.setPaycondition(paycondition);
			tContract.setPersion(persion);
			tContract.setTaxtype(taxtype);
			tContract.setComments(comments);
			tContract.setContracttemplatekey(contracttemplatekey);
			tContract.setIsdeleted("0");
			tContract.setStatus("0");
			tContract.setIsNewRecord(true);
		}else if(contractType.equals("1")){
			tContract = tContractService.findUniqueByProperty("ContractNo",contractNo);
			tContract.setContractname(contractname);
			tContract.setConcludeDate(concludeDate);
			tContract.setSupno(supno);
			tContract.setSupname(supname);
			tContract.setPurtype(purtype);
			tContract.setBusinesstype(businesstype);
			tContract.setContracttype(contracttype);
			tContract.setDepartment(department);
			tContract.setProarrivedate(proarrivedate);
			tContract.setTaxrate(taxrate);
			tContract.setCurrency(currency);
			tContract.setExchangerate(exchangerate);
			tContract.setPaycondition(paycondition);
			tContract.setPersion(persion);
			tContract.setTaxtype(taxtype);
			tContract.setComments(comments);
			tContract.setContracttemplatekey(contracttemplatekey);
			tContract.setIsdeleted("0");
			tContract.setId(id);
			tContract.setIsNewRecord(false);
		}else{
			tContract = tContractService.findUniqueByProperty("ContractNo",contractNo);
			tContract.setContractname(contractname);
			tContract.setConcludeDate(concludeDate);
			tContract.setSupno(supno);
			tContract.setSupname(supname);
			tContract.setPurtype(purtype);
			tContract.setBusinesstype(businesstype);
			tContract.setContracttype(contracttype);
			tContract.setDepartment(department);
			tContract.setProarrivedate(proarrivedate);
			tContract.setTaxrate(taxrate);
			tContract.setCurrency(currency);
			tContract.setExchangerate(exchangerate);
			tContract.setPaycondition(paycondition);
			tContract.setPersion(persion);
			tContract.setTaxtype(taxtype);
			tContract.setComments(comments);
			tContract.setContracttemplatekey(contracttemplatekey);
			tContract.setIsdeleted("0");
			tContract.setStatus("3");
			tContract.setId(id);
			tContract.setIsNewRecord(false);
		}
		//新增或编辑表单保存
		tContractService.save(tContract);//保存
		j.setSuccess(true);
		j.setBody(map);
		j.setMsg("保存合同管理成功");
		return j;
	}

	/**
	 * 保存合同管理
	 */
	@ResponseBody
	@RequiresPermissions(value={"contract:tContract:add","contract:tContract:edit"},logical=Logical.OR)
	@RequestMapping(value = "saveDetail")
	public AjaxJson saveDetail(HttpServletRequest request, Model model) throws Exception{
		AjaxJson j = new AjaxJson();

		String id = request.getParameter("id");
        String supno = request.getParameter("supno");
		String materialno = request.getParameter("materialno");
		String planarrivaldate = request.getParameter("planarrivaldate");
		String taxrate = request.getParameter("taxrate");

		for(TContractDetails tContractDetails:tContractDetailsList){
			if(tContractDetails.getMaterialno().equals(materialno)){
				j.setSuccess(false);
				j.setMsg("已经添加了这个合同标的");
				return j;
			}
		}

		TContractDetails tContractDetails = tContractService.findContractDetailByMaterialNo(materialno,supno);
		BigDecimal price = tContractDetails.getPrice();
		String quantity = tContractDetails.getQuantity();
		BigDecimal quantityBigDecimal = new BigDecimal(quantity);
		BigDecimal taxrateBigDecimal = new BigDecimal(taxrate);
		BigDecimal priceMoney = price.multiply(quantityBigDecimal);
		BigDecimal taxPrice = price.multiply(taxrateBigDecimal.add(new BigDecimal("1")));
		BigDecimal taxMoney = taxrateBigDecimal.multiply(quantityBigDecimal);
		BigDecimal totalMoney = priceMoney.add(taxMoney);
		tContractDetails.setPricemoney(priceMoney);
		tContractDetails.setTaxprice(taxPrice);
		tContractDetails.setTaxmoney(taxMoney);
		tContractDetails.setTotalmoney(totalMoney);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		tContractDetails.setPlanarrivaldate(sdf.parse(planarrivaldate));
		tContractDetails.setContractno(contractNo);
		tContractDetailsList.add(tContractDetails);

		j.setSuccess(true);
		j.setMsg("保存合同标的成功");
		return j;
	}

	@ResponseBody
	@RequestMapping(value = "removePayplan")
	public AjaxJson removePayplan(String paymentno) throws Exception{
		AjaxJson j = new AjaxJson();
		String[] nos = paymentno.split(",");
		List<TContractPayPlan> list = new ArrayList<TContractPayPlan>();
		for(String no : nos){
			for(TContractPayPlan tContractPayPlan : tContractPayPlanList){
				if(tContractPayPlan.getPaymentno().equals(no)){
					list.add(tContractPayPlan);
					break;
				}
			}
		}
		tContractPayPlanList.removeAll(list);
		j.setMsg("删除付款计划成功");
		return j;
	}

	@ResponseBody
	@RequestMapping(value = "deleteContractItem")
	public AjaxJson deleteContractItem(String nos) throws Exception{
		AjaxJson j = new AjaxJson();
		String[] noArr = nos.split(",");
		List<TContractAttach> list = new ArrayList<TContractAttach>();
		for(String no : noArr){
			for(TContractAttach tContractAttach : tContractAttachList){
				if(tContractAttach.getAnnexno().equals(no)){
					list.add(tContractAttach);
					break;
				}
			}
		}
		tContractAttachList.removeAll(list);
		j.setMsg("删除付款计划成功");
		return j;
	}

	@ResponseBody
	@RequestMapping(value = "savePayplan")
	public AjaxJson savePayplan(HttpServletRequest request, Model model) throws Exception{
		AjaxJson j = new AjaxJson();
		String paymentdate = new String(request.getParameter("paymentdate").getBytes("ISO-8859-1"),"UTF-8");
		String paymentproportion = new String(request.getParameter("paymentproportion").getBytes("ISO-8859-1"),"UTF-8");
		String paymentmoney = new String(request.getParameter("paymentmoney").getBytes("ISO-8859-1"),"UTF-8");
		String settlementmethod = new String(request.getParameter("settlementmethod").getBytes("ISO-8859-1"),"UTF-8");
		String termpayment = new String(request.getParameter("termpayment").getBytes("ISO-8859-1"),"UTF-8");
		String comment = new String(request.getParameter("comment").getBytes("ISO-8859-1"),"UTF-8");

		if(paymentdate.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写计划付款日期");
			return j;
		}
		if(paymentproportion.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写付款比例");
			return j;
		}
		if(paymentmoney.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写付款金额");
			return j;
		}
		if(settlementmethod.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写结算方式");
			return j;
		}
		if(termpayment.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写付款条件");
			return j;
		}
		if(comment.equals("")){
			j.setSuccess(false);
			j.setMsg("请填写付款说明");
			return j;
		}

		TContractPayPlan tContractPayPlan = new TContractPayPlan();
		String paymentno = tContractService.queryContractPayPlanNo(tContractPayPlan);
		tContractPayPlan.setContractno(contractNo);
		tContractPayPlan.setPaymentno(paymentno);
		tContractPayPlan.setPaymentdate(paymentdate);
		tContractPayPlan.setPaymentmoney(paymentmoney);
		tContractPayPlan.setPaymentproportion(paymentproportion);
		tContractPayPlan.setSettlementmethod(settlementmethod);
		tContractPayPlan.setTermpayment(termpayment);
		tContractPayPlan.setComment(comment);
		tContractPayPlan.setIsdeleted("0");
		tContractPayPlan.setIsNewRecord(true);
		tContractPayPlanList.add(tContractPayPlan);
		j.setSuccess(true);
		j.setMsg("保存合同付款计划成功");
		return j;
	}
	
	/**
	 * 删除合同管理
	 */
	@ResponseBody
	@RequiresPermissions("contract:tContract:del")
	@RequestMapping(value = "delete")
	public AjaxJson delete(TContract tContract) {
		AjaxJson j = new AjaxJson();
		tContractService.delete(tContract);
		j.setMsg("删除合同管理成功");
		return j;
	}
	
	/**
	 * 批量删除合同管理
	 */
	@ResponseBody
	@RequiresPermissions("contract:tContract:del")
	@RequestMapping(value = "deleteAll")
	public AjaxJson deleteAll(String ids) {
		AjaxJson j = new AjaxJson();
		String idArray[] =ids.split(",");
		for(String id : idArray){
			TContract tContract = tContractService.findUniqueByProperty("contractno",id);
			tContract.setIsNewRecord(false);
			tContract.setIsdeleted("1");
			tContractService.save(tContract);
		}
		j.setMsg("删除合同管理成功");
		return j;
	}
	
	/**
	 * 导出excel文件
	 */
	@ResponseBody
	@RequiresPermissions("contract:tContract:export")
    @RequestMapping(value = "export")
    public AjaxJson exportFile(TContract tContract, HttpServletRequest request, HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		try {
            String fileName = "合同管理"+DateUtils.getDate("yyyyMMddHHmmss")+".xlsx";
            Page<TContract> page = tContractService.findPage(new Page<TContract>(request, response, -1), tContract);
    		new ExportExcel("合同管理", TContract.class).setDataList(page.getList()).write(response, fileName).dispose();
    		j.setSuccess(true);
    		j.setMsg("导出成功！");
    		return j;
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("导出合同管理记录失败！失败信息："+e.getMessage());
		}
			return j;
    }

	/**
	 * 导入Excel数据

	 */
	@ResponseBody
	@RequiresPermissions("contract:tContract:import")
    @RequestMapping(value = "import")
   	public AjaxJson importFile(@RequestParam("file")MultipartFile file, HttpServletResponse response, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		try {
			int successNum = 0;
			int failureNum = 0;
			StringBuilder failureMsg = new StringBuilder();
			ImportExcel ei = new ImportExcel(file, 1, 0);
			List<TContract> list = ei.getDataList(TContract.class);
			for (TContract tContract : list){
				try{
					tContractService.save(tContract);
					successNum++;
				}catch(ConstraintViolationException ex){
					failureNum++;
				}catch (Exception ex) {
					failureNum++;
				}
			}
			if (failureNum>0){
				failureMsg.insert(0, "，失败 "+failureNum+" 条合同管理记录。");
			}
			j.setMsg( "已成功导入 "+successNum+" 条合同管理记录"+failureMsg);
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg("导入合同管理失败！失败信息："+e.getMessage());
		}
		return j;
    }
	
	/**
	 * 下载导入合同管理数据模板
	 */
	@ResponseBody
	@RequiresPermissions("contract:tContract:import")
    @RequestMapping(value = "import/template")
     public AjaxJson importFileTemplate(HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		try {
            String fileName = "合同管理数据导入模板.xlsx";
    		List<TContract> list = Lists.newArrayList(); 
    		new ExportExcel("合同管理数据", TContract.class, 1).setDataList(list).write(response, fileName).dispose();
    		return null;
		} catch (Exception e) {
			j.setSuccess(false);
			j.setMsg( "导入模板下载失败！失败信息："+e.getMessage());
		}
		return j;
    }

    /**
     * 获取历史供货列表
     * @param materialNo
     * @param materialName
     * @param supName
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String,Object> getHistorySupply(String materialNo,String materialName,String supName,Integer pageNo,Integer pageSize){
	    Map<String,Object> map = tContractService.getHistorySupply(materialNo,materialName,supName,pageNo,pageSize);
	    return map;
    }

}