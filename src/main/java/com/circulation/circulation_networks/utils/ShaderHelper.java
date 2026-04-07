package com.circulation.circulation_networks.utils;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import net.minecraft.client.Minecraft;
//~ mc_imports
import net.minecraft.util.ResourceLocation;
//? if <1.20 {
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
//?} else if <1.21 {
/*import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
*///?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?}
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

//~ if >=1.20 '@SideOnly(Side.CLIENT)' -> '@OnlyIn(Dist.CLIENT)' {
@SideOnly(Side.CLIENT)
//~}
public final class ShaderHelper {

    private ShaderHelper() {
    }

    public static int loadShader(ResourceLocation vertexLoc, ResourceLocation fragmentLoc, String... attribNames) {
        String vertSource = readResource(vertexLoc);
        String fragSource = readResource(fragmentLoc);
        if (vertSource == null || fragSource == null) return 0;

        int vert = compileShader(GL20.GL_VERTEX_SHADER, vertSource, vertexLoc.toString());
        int frag = compileShader(GL20.GL_FRAGMENT_SHADER, fragSource, fragmentLoc.toString());
        if (vert == 0 || frag == 0) {
            if (vert != 0) GL20.glDeleteShader(vert);
            if (frag != 0) GL20.glDeleteShader(frag);
            return 0;
        }

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vert);
        GL20.glAttachShader(program, frag);

        for (int i = 0; i < attribNames.length; i++) {
            GL20.glBindAttribLocation(program, i, attribNames[i]);
        }

        GL20.glLinkProgram(program);
        GL20.glDeleteShader(vert);
        GL20.glDeleteShader(frag);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program, 8192);
            CirculationFlowNetworks.LOGGER.error("Shader program link failed [{}+{}]: {}", vertexLoc, fragmentLoc, log);
            GL20.glDeleteProgram(program);
            return 0;
        }

        return program;
    }

    public static void deleteProgram(int program) {
        if (program != 0) {
            GL20.glDeleteProgram(program);
        }
    }

    public static int compileInline(String vertSource, String fragSource, String... attribNames) {
        int vert = compileShader(GL20.GL_VERTEX_SHADER, vertSource, "inline-vert");
        int frag = compileShader(GL20.GL_FRAGMENT_SHADER, fragSource, "inline-frag");
        if (vert == 0 || frag == 0) {
            if (vert != 0) GL20.glDeleteShader(vert);
            if (frag != 0) GL20.glDeleteShader(frag);
            return 0;
        }
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vert);
        GL20.glAttachShader(program, frag);
        for (int i = 0; i < attribNames.length; i++) {
            GL20.glBindAttribLocation(program, i, attribNames[i]);
        }
        GL20.glLinkProgram(program);
        GL20.glDeleteShader(vert);
        GL20.glDeleteShader(frag);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program, 8192);
            CirculationFlowNetworks.LOGGER.error("Inline shader link failed: {}", log);
            GL20.glDeleteProgram(program);
            return 0;
        }
        return program;
    }

    private static int compileShader(int type, String source, String name) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, 8192);
            CirculationFlowNetworks.LOGGER.error("Shader compile failed [{}]: {}", name, log);
            GL20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static @Nullable String readResource(ResourceLocation loc) {
        //? if <1.20 {
        try (InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
             //?} else {
            /*try (InputStream is = Minecraft.getInstance().getResourceManager().getResourceOrThrow(loc).open();
             *///?}
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            CirculationFlowNetworks.LOGGER.error("Failed to load shader resource: {}", loc, e);
            return null;
        }
    }
}
