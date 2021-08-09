package xyz.xenondevs.vetric.transformer.obfuscation.misc

import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import xyz.xenondevs.vetric.config.type.TransformerConfig
import xyz.xenondevs.vetric.jvm.ClassWrapper
import xyz.xenondevs.vetric.transformer.ClassTransformer
import xyz.xenondevs.vetric.transformer.TransformerPriority.HIGHEST
import xyz.xenondevs.vetric.util.asm.*
import xyz.xenondevs.vetric.util.filterTypeAnd

/**
 * Replaces enum constant ``GETSTATIC (0xb2)`` instructions with
 * method calls to Enum.valueOf. This will slow down the overall
 * process, but when combined with a StringEncryption this will
 * hide which enum constant is referenced (even library enums).
 */
object EnumAccessHider : ClassTransformer("EnumAccessHider", TransformerConfig(EnumAccessHider::class), HIGHEST) {
    
    override fun transformMethod(method: MethodNode) {
        // The enum is not fully initialized yet so "Enum.ValueOf" will throw an exception.
        // TODO this should be moved to the forEach call below to only skip constants of the current enum
        if (currentClass.isEnum() && method.name.equals("<clinit>"))
            return
        method.instructions
            .filterTypeAnd<FieldInsnNode> { it.opcode == GETSTATIC && it.ownerWrapper.isEnum() && it.access.isEnum() }
            .forEach {
                method.instructions.replace(it, insnBuilder {
                    ldc(ASMUtils.getType(it.owner))
                    ldc(it.name)
                    invokestatic("java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;")
                    checkcast(it.owner)
                })
            }
    }
    
    override fun transformClass(clazz: ClassWrapper) = Unit
    
    override fun transformField(field: FieldNode) = Unit
}