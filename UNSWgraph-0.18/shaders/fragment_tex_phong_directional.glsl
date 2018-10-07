
out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;

// Light properties
//uniform vec3 lightPos;
uniform vec3 lightDirection;
uniform vec3 lightIntensity;
uniform vec3 ambientIntensity;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;
uniform vec3 specularCoeff;
uniform float phongExp;

uniform sampler2D tex;

in vec4 viewPosition;
in vec3 m;
in vec3 n; ////TEST

in vec2 texCoordFrag;

void main()
{
	vec3 normal = m;
    // Compute the s, v and r vectors
    //vec3 lightDir = normalize(view_matrix*vec4(lightDirection,1)).xyz; // test light direction for specular
    vec3 lightDir = normalize(lightDirection);
    vec3 viewDir = normalize(-viewPosition.xyz);
    
    vec3 ambient = ambientIntensity*ambientCoeff;
    vec3 diffuse = max(lightIntensity*diffuseCoeff*dot(normal,lightDir), 0.0);
    
    // specular shading
    vec3 reflectDir = normalize(reflect(-lightDir,m)); // Original reflectDir computation
    // vec3 reflectDir = normalize(reflect(-sn,m)); // Test reflectDir
    vec3 specular = max(lightIntensity*specularCoeff*pow(dot(viewDir, reflectDir),phongExp), 0.0);

    vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);

    outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
}
