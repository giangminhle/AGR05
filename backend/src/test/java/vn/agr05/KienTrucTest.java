package vn.agr05;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Luật phụ thuộc một chiều api → bll → dal → entity (CLAUDE.md, ARCH01). */
@AnalyzeClasses(packages = "vn.agr05", importOptions = ImportOption.DoNotIncludeTests.class)
class KienTrucTest {

    @ArchTest
    static final ArchRule phanTang = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Api").definedBy("vn.agr05.api..")
            .layer("Bll").definedBy("vn.agr05.bll..")
            .layer("Dal").definedBy("vn.agr05.dal..")
            .layer("Entity").definedBy("vn.agr05.entity..")
            .layer("Config").definedBy("vn.agr05.config..")
            .whereLayer("Api").mayNotBeAccessedByAnyLayer()
            .whereLayer("Bll").mayOnlyBeAccessedByLayers("Api")
            .whereLayer("Dal").mayOnlyBeAccessedByLayers("Bll")
            .whereLayer("Entity").mayOnlyBeAccessedByLayers("Api", "Bll", "Dal")
            .whereLayer("Config").mayOnlyBeAccessedByLayers("Bll");

    @ArchTest
    static final ArchRule apiKhongDungJdbc = noClasses().that().resideInAPackage("vn.agr05.api..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.jdbc..");

    @ArchTest
    static final ArchRule chiDalCoJdbc = noClasses().that().resideOutsideOfPackage("vn.agr05.dal..")
            .should().dependOnClassesThat().haveSimpleName("NamedParameterJdbcTemplate");

    @ArchTest
    static final ArchRule chiAIServiceGoiSmile = noClasses().that().resideOutsideOfPackage("vn.agr05.bll..")
            .should().dependOnClassesThat().resideInAPackage("smile..");

    @ArchTest
    static final ArchRule entityKhongPhuThuocSpring = noClasses().that().resideInAPackage("vn.agr05.entity..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");
}
