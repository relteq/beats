<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" version="1.0" indent="yes"/>

<xsl:template match="*">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>

<xsl:template match="@*">
	<xsl:copy/>
</xsl:template>

<xsl:template match="text()">
	<xsl:if test="string-length(normalize-space(.))&gt;0"><xsl:copy/></xsl:if>
</xsl:template>

<xsl:template match="*" mode="params">
	<xsl:apply-templates select="@*" mode="params"/>
	<xsl:apply-templates select="*" mode="params"/>
</xsl:template>

<xsl:template match="@*" mode="params"/>

<xsl:template match="network/DirectionsCache"/>

<xsl:template match="link/@name|link/@road_name|link/@record"/>
<xsl:template match="link/description"/>

<xsl:template match="link">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsl:if test="@road_name">
			<roads>
				<road name="{@road_name}"/>
			</roads>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>

<xsl:template match="node/@name|node/description"/>

<xsl:template match="node">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsl:if test="@name">
			<roadway_markers>
				<marker name="{@name}"/>
			</roadway_markers>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>

<xsl:template match="sensor/@link_type|sensor/description|sensor/position"/>

<xsl:template match="sensor|event">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates/>
		<xsl:if test="not(parameters)">
			<parameters>
				<xsl:apply-templates select="." mode="params"/>
			</parameters>
		</xsl:if>
	</xsl:copy>
</xsl:template>

<xsl:template match="sensor/data_sources">
	<table name="{local-name()}">
		<column_names>
			<column_name name="url" key="false"/>
			<column_name name="dt" key="false"/>
			<column_name name="format" key="false"/>
		</column_names>
		<xsl:apply-templates select="data_source" mode="table"/>
	</table>
</xsl:template>

<xsl:template match="data_source" mode="table">
	<row>
		<column><xsl:value-of select="@url"/></column>
		<column><xsl:value-of select="@dt"/></column>
		<column><xsl:value-of select="@format"/></column>
	</row>
</xsl:template>

<xsl:template match="parameters">
	<xsl:copy>
		<xsl:apply-templates select="*"/>
		<xsl:apply-templates select=".." mode="params"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="event/@reset_to_nominal|event/fundamentalDiagram|event/lane_count_change"/>

<xsl:template match="sensor/@link_type|event/@reset_to_nominal|event/fundamentalDiagram/@*" mode="params">
	<parameter name="{name()}" value="{string(.)}"/>
</xsl:template>

<xsl:template match="event/lane_count_change" mode="params">
	<parameter name="{local-name()}" value="{@delta}"/>
</xsl:template>

<xsl:template match="fundamentalDiagram">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsl:if test="not(@critical_speed)">
			<xsl:attribute name="critical_speed"><xsl:value-of select="@free_flow_speed"/></xsl:attribute>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>

<xsl:template match="controller">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsl:if test="not(@enabled)">
			<xsl:attribute name="enabled">true</xsl:attribute>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="parameters">
				<xsl:apply-templates select="parameters"/>
			</xsl:when>
			<xsl:when test="PlanSequence">
				<parameters>
					<xsl:apply-templates select="." mode="params"/>
				</parameters>
			</xsl:when>
		</xsl:choose>
		<xsl:apply-templates select="display_position"/>
		<xsl:apply-templates select="targetElements"/>
		<xsl:apply-templates select="feedbackElements"/>
		<xsl:apply-templates select="queue_controller"/>
		<xsl:apply-templates select="ActivationIntervals"/>
		<xsl:apply-templates select="table"/>
		<xsl:apply-templates select="PlanSequence|PlanList" mode="table"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="PlanSequence" mode="table">
	<table name="Plan Sequence">
		<column_names>
			<column_name name="Plan ID" key="true"/>
			<column_name name="Start Time" key="false"/>
			<column_name name="Cycle Length" key="false"/>
		</column_names>
		<xsl:apply-templates select="plan_reference" mode="table"/>
	</table>
</xsl:template>

<xsl:template match="plan_reference" mode="table">
	<row>
		<column><xsl:value-of select="@plan_id"/></column>
		<column><xsl:value-of select="@start_time"/></column>
		<column><xsl:value-of select="../../PlanList/plan[@id=current()/@plan_id]/@cyclelength"/></column>
	</row>
</xsl:template>

<xsl:template match="PlanList" mode="table">
	<table name="Plan List">
		<column_names>
			<column_name name="Plan ID" key="true"/>
			<column_name name="Intersection" key="true"/>
			<column_name name="Offset" key="false"/>
			<column_name name="Movement A" key="true"/>
			<column_name name="Movement B" key="true"/>
			<column_name name="Green Time" key="false"/>
		</column_names>
		<xsl:apply-templates select="plan/intersection/stage" mode="table"/>
	</table>
</xsl:template>

<xsl:template match="plan/intersection/stage" mode="table">
	<row>
		<column><xsl:value-of select="../../@id"/></column>
		<column><xsl:value-of select="../@node_id"/></column>
		<column><xsl:value-of select="../@offset"/></column>
		<column><xsl:value-of select="@movA"/></column>
		<column><xsl:value-of select="@movB"/></column>
		<column><xsl:value-of select="@greentime"/></column>
	</row>
</xsl:template>

<xsl:template match="PlanSequence" mode="params">
	<parameter name="Transition Delay" value="{@transition_delay}"/>
</xsl:template>

<xsl:template match="phase/links">
	<link_references>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates/>
	</link_references>
</xsl:template>

<xsl:template match="column_name">
	<xsl:copy>
		<xsl:attribute name="name"><xsl:value-of select="string(.)"/></xsl:attribute>
		<xsl:attribute name="key">false</xsl:attribute>
	</xsl:copy>
</xsl:template>
</xsl:stylesheet>
