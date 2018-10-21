
out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;

// Light properties
//uniform vec3 lightDirection;
uniform vec3 lightPos;
uniform vec3 torchDirection;
uniform vec3 lightIntensity;
uniform vec3 ambientIntensity;
uniform float cutoff;
uniform float cutoffDistance;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;
uniform vec3 specularCoeff;
uniform float phongExp;

uniform sampler2D tex;

in vec4 viewPosition;
in vec3 m;

in vec2 texCoordFrag;

void main()
{
	vec3 normal = m;
    // Compute the light direction and view direction vectors
    vec3 lightDir = normalize(view_matrix * vec4(lightPos, 1) - viewPosition).xyz;
    float lightDistance = distance(view_matrix * vec4(lightPos, 1), viewPosition);
    
    vec3 viewDir = normalize(-viewPosition.xyz);
    float theta = dot(lightDir,normalize(torchDirection));
    
    vec3 ambient = ambientIntensity*ambientCoeff;
    vec3 diffuse;
    vec3 reflectDir = normalize(reflect(-lightDir,m));
    vec3 specular;
    
    if(theta > cutoff && lightDistance < cutoffDistance){
    	
    	float maxDiff = 1.0 - cutoff;
    	float diff = 1.0 - theta;
    	float ratio = 1.0 - (diff/maxDiff);
    	
    	float Dist = 1.0 - (lightDistance/cutoffDistance); 
    	
    	diffuse = max(vec3(Dist,Dist,Dist)*vec3(ratio,ratio,ratio)*lightIntensity*diffuseCoeff*dot(normal,lightDir), 0.0);
    	
    	if (dot(normal,lightDir) > 0) 
    		specular = max(vec3(Dist,Dist,Dist)*lightIntensity*specularCoeff*pow(dot(viewDir, reflectDir),phongExp), 0.0);
    	else
			specular = vec3(0);
    	
    }else{
    	diffuse = vec3(0);
    	specular = vec3(0);
	}


    vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);

    outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
}
