package husacct.define.domain.services;

import husacct.ServiceProvider;
import husacct.define.domain.SoftwareArchitecture;
import husacct.define.domain.SoftwareUnitRegExDefinition;
import husacct.define.domain.module.ModuleStrategy;
import husacct.define.domain.services.stateservice.StateService;
import husacct.define.domain.softwareunit.ExpressionUnitDefinition;
import husacct.define.domain.softwareunit.SoftwareUnitDefinition;
import husacct.define.domain.softwareunit.SoftwareUnitDefinition.Type;
import husacct.define.task.DefinitionController;
import husacct.define.task.JtreeController;
import husacct.define.task.components.AbstractCombinedComponent;
import husacct.define.task.components.AnalyzedModuleComponent;
import husacct.define.task.components.RegexComponent;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class SoftwareUnitDefinitionDomainService {

	public void addSoftwareUnitsToModule(long moduleId, ArrayList<AnalyzedModuleComponent> units) {
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
		if (module != null) {
			try {
				for (AnalyzedModuleComponent softwareunit : units) {
					Type type = Type.valueOf(softwareunit.getType());
					SoftwareUnitDefinition unit = new SoftwareUnitDefinition(softwareunit.getUniqueName(), type);
					//Logger.getLogger(SoftwareUnitDefinitionDomainService.class).info("cheking if regex wrapper " + softwareunit.getType() + "ok " + softwareunit.getUniqueName());
					if (softwareunit instanceof RegexComponent) {
						module.addSUDefinition(unit);
						RegisterRegixSoftwareUnits((RegexComponent) softwareunit, module, unit);
						JtreeController.instance().removeRegexTreeItem((RegexComponent) softwareunit);
					} else {
						module.addSUDefinition(unit);
						JtreeController.instance().removeTreeItem(softwareunit);
					}
				}
				WarningMessageService.getInstance().processModule(module);
			} catch (Exception e) {
				Logger.getLogger(SoftwareUnitDefinitionDomainService.class).error(e.getMessage());
				// System.out.println(e.getStackTrace());
			}
			ServiceProvider.getInstance().getDefineService().notifyServiceListeners();
		}
	}

	public ArrayList<SoftwareUnitDefinition> getSoftwareUnit(long moduleId) {
		ArrayList<SoftwareUnitDefinition> softwareUnits = null;
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
		if (module != null) {
			softwareUnits = module.getUnits();
		}
		return softwareUnits;
	}

	// Returns null, if no SoftwareUnit with softwareUnitName is mapped to a ModuleStrategy	
	public SoftwareUnitDefinition getSoftwareUnitByName(String softwareUnitName) {
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleBySoftwareUnit(softwareUnitName);
		SoftwareUnitDefinition softwareUnit = null;
		if (module != null){
			softwareUnit = module.getSoftwareUnitByName(softwareUnitName);
		}	
		return softwareUnit;
	}

	public ArrayList<String> getSoftwareUnitNames(long moduleId) {
		ArrayList<String> softwareUnitNames = new ArrayList<String>();
		try {
			ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
			if (module != null) {
				ArrayList<SoftwareUnitDefinition> softwareUnits = module.getUnits();
				for (SoftwareUnitDefinition unit : softwareUnits) {
					softwareUnitNames.add(unit.getName());
				}
			}
		} catch (Exception e) {
			Logger.getLogger(SoftwareUnitDefinitionDomainService.class).error(e.getMessage());
		}
		return softwareUnitNames;
	}

	// Returns "", if no SoftwareUnit with softwareUnitName is mapped to a ModuleStrategy	
	public String getSoftwareUnitType(String softwareUnitName) {
		SoftwareUnitDefinition unit = getSoftwareUnitByName(softwareUnitName);
		String softwareUnitType = "";
		if (unit != null){
			softwareUnitType = unit.getType().toString();
		}
		return softwareUnitType;
	}

	public void removeSoftwareUnit(long moduleId, String softwareUnit) {
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
		if (module != null){
			SoftwareUnitDefinition unit = getSoftwareUnitByName(softwareUnit);
			if (unit != null){
				module.removeSUDefintion(unit);
				StateService.instance().removeSoftwareUnit(module, unit);
				ServiceProvider.getInstance().getDefineService().notifyServiceListeners();
			}
		}
	}

	public void removeSoftwareUnit(long moduleId, ArrayList<AnalyzedModuleComponent> data) {
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
		if (module != null){
			for (AnalyzedModuleComponent units : data) {
				SoftwareUnitDefinition unit = getSoftwareUnitByName(units.getUniqueName());
				if(unit != null){
					module.removeSUDefintion(unit);
					WarningMessageService.getInstance().processModule(module);
					StateService.instance().removeSoftwareUnit(module, unit);
					ServiceProvider.getInstance().getDefineService().notifyServiceListeners();
				}
			}
		}
	}

	public void changeSoftwareUnit(long from, long to, ArrayList<String> names) {
		SoftwareArchitecture.getInstance().changeSoftwareUnit(from,to,names);
		DefinitionController.getInstance().notifyObservers();
	}
	
	// Regex Services
	
	public void addRegexToModule(long moduleId, ArrayList<AnalyzedModuleComponent> softwareUnits, String regExName) {
		// Note 2015/07: softwareUnits is not used!
		try {
			RegexComponent regixwrapper = JtreeController.instance().registerRegix(regExName);
			addRegExUnitsToModule(moduleId, regixwrapper);
		} catch (Exception e) {
			Logger.getLogger(SoftwareUnitDefinitionDomainService.class).error("Undefined softwareunit");
			Logger.getLogger(SoftwareUnitDefinitionDomainService.class).error(e.getMessage());
		}
		ServiceProvider.getInstance().getDefineService().notifyServiceListeners();
	}

	public void editRegex(long selectedModuleId, ArrayList<AnalyzedModuleComponent> components, String editingRegEx) {
		removeRegExSoftwareUnit(selectedModuleId, editingRegEx);
		RegexComponent tobesaved = JtreeController.instance().createRegexRepresentation(editingRegEx, components);
		addRegExUnitsToModule(selectedModuleId, tobesaved);
	}

	public ExpressionUnitDefinition getExpressionByName(long ModuleId, String name) {
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(ModuleId);
		if (module != null) {
			return (ExpressionUnitDefinition) module.getSoftwareUnitByName(name);
		} else {
			return null;
		}
	}

	// Returns null, if no SoftwareUnit with softwareUnitName is mapped to a ModuleStrategy	
	public SoftwareUnitRegExDefinition getRegExSoftwareUnitByName(String softwareUnitName) {
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleByRegExSoftwareUnit(softwareUnitName);
		SoftwareUnitRegExDefinition softwareUnit = null;
		if (module != null){
			softwareUnit = module.getRegExSoftwareUnitByName(softwareUnitName);
		}
		return softwareUnit;
	}

	public ArrayList<String> getRegExSoftwareUnitNames(long moduleId) {
		ArrayList<String> softwareUnitNames = new ArrayList<String>();
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
		if (module != null){
			ArrayList<SoftwareUnitRegExDefinition> softwareUnits = module.getRegExUnits();
			for (SoftwareUnitRegExDefinition unit : softwareUnits) {
				softwareUnitNames.add(unit.getName());
			}
		}
		return softwareUnitNames;
	}

	private void RegisterRegixSoftwareUnits(RegexComponent softwareunit,
			ModuleStrategy parent, SoftwareUnitDefinition rootunit) {
	}

	// Returns null, if no SoftwareUnit with softwareUnitName is mapped to a ModuleStrategy	
	public ExpressionUnitDefinition removeRegExSoftwareUnit(long moduleId, String softwareUnit) {
		SoftwareUnitDefinition unit = getSoftwareUnitByName(softwareUnit);
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
		if (module != null){
			if(unit != null){
				module.removeSUDefintion(unit);
				JtreeController.instance().restoreRegexWrapper((ExpressionUnitDefinition) unit);
				ServiceProvider.getInstance().getDefineService().notifyServiceListeners();
			}
		}
		return (ExpressionUnitDefinition) unit;
	}

	private void addRegExUnitsToModule(long moduleId, AnalyzedModuleComponent softwareunit) {
		ModuleStrategy module = SoftwareArchitecture.getInstance().getModuleById(moduleId);
		if (module != null) {
			try {
				if (softwareunit instanceof RegexComponent) {
					ExpressionUnitDefinition ex = new ExpressionUnitDefinition(softwareunit.getUniqueName(), SoftwareUnitDefinition.Type.REGEX);
					for (AbstractCombinedComponent ir : ((RegexComponent) softwareunit).getChildren()) {
						Type typet = Type.valueOf(ir.getType());
						SoftwareUnitDefinition unitt = new SoftwareUnitDefinition(ir.getUniqueName(), typet);
						ex.addSoftwareUnit(unitt);
					}
					StateService.instance().registerAnalyzedUnit(softwareunit);
	                JtreeController.instance().removeTreeItem(softwareunit);
					module.addSUDefinition(ex);
				} else {
					Type type = Type.valueOf(softwareunit.getType());
					SoftwareUnitDefinition unit = new SoftwareUnitDefinition(softwareunit.getUniqueName(), type);
					module.addSUDefinition(unit);
					JtreeController.instance().getTree().removeTreeItem(softwareunit);
				}
				WarningMessageService.getInstance().processModule(module);
			} catch (Exception e) {
				Logger.getLogger(SoftwareUnitDefinitionDomainService.class).error(
						"Undefined softwareunit type: " + softwareunit.getType());
				Logger.getLogger(SoftwareUnitDefinitionDomainService.class).error(
						e.getMessage());
				// System.out.println(e.getStackTrace());
			}
			ServiceProvider.getInstance().getDefineService().notifyServiceListeners();
		}
	}


}
