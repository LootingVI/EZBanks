package de.flori.ezbanks.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;

@SuppressWarnings({"unused", "UnusedReturnValue", "deprecation"})
public class ItemBuilder {

    private final Map<UUID, String> textureCache;
    private final ItemStack item;
    private final ItemMeta meta;

    /**
     * Initializes the ItemBuilder with {@link org.bukkit.inventory.ItemStack}
     *
     * @param item The ItemStack
     */
    public ItemBuilder(final ItemStack item) {
        this.textureCache = new HashMap<>();
        this.item = item;
        this.meta = item.getItemMeta();
    }

    /**
     * Initializes the ItemBuilder with {@link org.bukkit.Material}
     *
     * @param material Material for the ItemStack
     */
    public ItemBuilder(final Material material) {
        this(material, 1);
    }

    /**
     * Initializes the ItemBuilder with {@link org.bukkit.Material} and Amount
     *
     * @param material Material for the ItemStack
     * @param amount   Amount for the ItemStack
     */
    public ItemBuilder(final Material material, final int amount) {
        this.textureCache = new HashMap<>();
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    /**
     * Set the Durability for the ItemStack
     *
     * @param durability new Durability for the ItemStack
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setDurability(final int durability) {
        item.setDurability((short) durability);
        return this;
    }

    /**
     * Add an Enchantment to the ItemStack
     *
     * @param enchantment Enchantment for the ItemStack
     * @param level       Level for the Enchantment
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addEnchantment(final Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * Add Enchantments to the ItemStack
     *
     * @param enchantments Enchantments for the ItemStack
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addEnchantment(final Map<Enchantment, Integer> enchantments) {
        enchantments.forEach(this::addEnchantment);
        return this;
    }

    /**
     * Remove Enchantments from the ItemStack
     *
     * @param enchantment Enchantment for the ItemStack
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeEnchantment(final Enchantment enchantment) {
        meta.removeEnchant(enchantment);
        return this;
    }

    /**
     * Add ItemFlags to the ItemStack
     *
     * @param flags ItemFlags for the ItemStack
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addItemFlags(final ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    /**
     * Remove ItemFlags from the ItemStack
     *
     * @param flags ItemFlags for the ItemStack
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeItemFlags(final ItemFlag... flags) {
        meta.removeItemFlags(flags);
        return this;
    }

    /**
     * Add an Attribute Modifier to the ItemStack
     *
     * @param attribute The attribute to modify
     * @param modifier The modifier to apply
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addAttributeModifier(final Attribute attribute, final AttributeModifier modifier) {
        meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    /**
     * Remove an Attribute Modifier from the ItemStack
     *
     * @param attribute The attribute to remove
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeAttributeModifier(final Attribute attribute) {
        meta.removeAttributeModifier(attribute);
        return this;
    }

    /**
     * Clear all Attribute Modifiers from the ItemStack
     *
     * @return {@link ItemBuilder}
     */
    public ItemBuilder clearAttributeModifiers() {
        Arrays.stream(Attribute.values()).forEach(this::removeAttributeModifier);
        return this;
    }

    /**
     * Set the Custom Model Data for the ItemStack
     *
     * @param data The Custom Model Data
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setCustomModelData(final int data) {
        meta.setCustomModelData(data);
        return this;
    }

    /**
     * Set a persistent data value for the ItemStack
     *
     * @param namespacedKey The key to set
     * @param dataType The data type to set
     * @param value The value to set
     * @return {@link ItemBuilder}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ItemBuilder setPersistentData(final NamespacedKey namespacedKey, PersistentDataType dataType, final Object value) {
        meta.getPersistentDataContainer().set(namespacedKey, dataType, value);
        return this;
    }

    /**
     * Remove a persistent data value from the ItemStack
     *
     * @param namespacedKey The key to remove
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removePersistentData(final NamespacedKey namespacedKey) {
        meta.getPersistentDataContainer().remove(namespacedKey);
        return this;
    }

    /**
     * Give the ItemStack a Skull Texture by PlayerProfile
     */
    public ItemBuilder setSkullOwner(final PlayerProfile playerProfile) {
        if (item.getType() == Material.PLAYER_HEAD) {
            final SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setPlayerProfile(playerProfile);
            item.setItemMeta(skullMeta);
        }

        return this;
    }

    /**
     * Give the ItemStack a Skull Texture
     *
     * @param texture The Skull Texture from Player
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setSkullOwner(final String texture) {
        if (item.getType() == Material.PLAYER_HEAD) {
            final UUID uuid = UUID.randomUUID();
            final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
            playerProfile.setProperty(new ProfileProperty("textures", texture));
            return setSkullOwner(playerProfile);
        }

        return this;
    }

    /**
     * Give the ItemStack a Skull Texture
     *
     * @deprecated use {@link ItemBuilder#setSkullOwner(Player)} instead
     * @param uuid The UUID from the Player
     * @return {@link ItemBuilder}
     */
    @Deprecated
    public ItemBuilder setSkullOwner(final UUID uuid) {
        if (textureCache.containsKey(uuid)) {
            setSkullOwner(textureCache.get(uuid));
            return this;
        }

        try {
            final HttpURLConnection connection = (HttpURLConnection) URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "")).toURL().openConnection();
            connection.setReadTimeout(5000);

            final JsonObject result = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
            final JsonArray properties = result.get("properties").getAsJsonArray();
            final String texture = properties.get(0).getAsJsonObject().get("value").getAsString();

            textureCache.put(uuid, texture);
            return setSkullOwner(texture);
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * Give the ItemStack a Skull Texture by Player
     * @param player The Player
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setSkullOwner(final Player player) {
        return setSkullOwner(player.getPlayerProfile());
    }

    /**
     * Set an Armor Trim for the Armor
     *
     * @param trim The Armor Trim
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setArmorTrim(final ArmorTrim trim) {
        if (item.getType().name().contains("HELMET") || item.getType().name().contains("CHESTPLATE") || item.getType().name().contains("LEGGINGS") || item.getType().name().contains("BOOTS")) {
            final ArmorMeta armorMeta = (ArmorMeta) meta;

            armorMeta.setTrim(trim);
            item.setItemMeta(armorMeta);
        }

        return this;
    }

    /**
     * Set a Pattern to a specific position
     *
     * @param position The position for the Pattern
     * @param pattern  The pattern for the Banner
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setPattern(final int position, final Pattern pattern) {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            final BannerMeta bannerMeta = (BannerMeta) meta;

            bannerMeta.setPattern(position, pattern);
            item.setItemMeta(bannerMeta);
        }

        return this;
    }

    /**
     * Add a Banner pattern
     *
     * @param pattern The pattern for the Banner
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addPattern(final Pattern pattern) {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            final BannerMeta bannerMeta = (BannerMeta) meta;

            bannerMeta.addPattern(pattern);
            item.setItemMeta(bannerMeta);
        }

        return this;
    }

    /**
     * Remove a Pattern from the Banner
     *
     * @param position The position from the Pattern
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removePattern(final int position) {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            final BannerMeta bannerMeta = (BannerMeta) meta;

            bannerMeta.removePattern(position);
            item.setItemMeta(bannerMeta);
        }

        return this;
    }

    /**
     * Set the content for a book page
     *
     * @param page    The book page
     * @param content The content for the book page
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setBookPage(final int page, final Component content) {
        if (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK) {
            final BookMeta bookMeta = (BookMeta) meta;

            bookMeta.page(page, content);
            item.setItemMeta(bookMeta);
        }
        return this;
    }

    /**
     * Add a new book page
     *
     * @param pages The book pages
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addBookPage(final Component... pages) {
        if (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK) {
            final BookMeta bookMeta = (BookMeta) meta;

            bookMeta.addPages(pages);
            item.setItemMeta(bookMeta);
        }

        return this;
    }

    public ItemBuilder setPotionType(final PotionType type) {
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            final PotionMeta potionMeta = (PotionMeta) meta;

            potionMeta.setBasePotionType(type);
            item.setItemMeta(potionMeta);
        }

        return this;
    }

    /**
     * Add an effect to a potion
     *
     * @param type      The potion type
     * @param strength  The amplifier
     * @param duration  The duration
     * @param overwrite Should the current effects be overwritten
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addPotionEffect(final PotionEffectType type, final int strength, final int duration, final boolean overwrite) {
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            final PotionMeta potionMeta = (PotionMeta) meta;

            potionMeta.addCustomEffect(new PotionEffect(type, strength, duration), overwrite);
            item.setItemMeta(potionMeta);
        }
        return this;
    }

    /**
     * The effect type
     *
     * @param type The potion type
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removePotionEffect(final PotionEffectType type) {
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            final PotionMeta potionMeta = (PotionMeta) meta;

            potionMeta.removeCustomEffect(type);
            item.setItemMeta(potionMeta);
        }

        return this;
    }

    /**
     * @return The Material from the ItemStack
     */
    public Material getType() {
        return item.getType();
    }

    /**
     * @return The Display Name from the ItemStack
     */
    public Component getDisplayName() {
        return meta.hasDisplayName() ? meta.displayName() : Component.empty();
    }

    /**
     * Set the Display Name for the ItemStack
     *
     * @param displayName new Display Name for the ItemStack
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setDisplayName(final Component displayName) {
        meta.displayName(displayName);
        return this;
    }

    /**
     * @return The Amount from the ItemStack
     */
    public int getAmount() {
        return item.getAmount();
    }

    /**
     * Set the Amount for the ItemStack
     *
     * @param amount new Amount for the ItemStack
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setAmount(final int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * @return The Lore List from the ItemStack
     */
    public List<Component> getLore() {
        return meta.hasLore() ? meta.lore() : new ArrayList<>();
    }

    /**
     * Set the Lore for the ItemStack
     *
     * @param lore List with Strings for the Item Lore
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setLore(final List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    /**
     * Set the Lore for the ItemStack
     *
     * @param lore String Array for the Item Lore
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setLore(final Component... lore) {
        setLore(Arrays.asList(lore));
        return this;
    }

    /**
     * @return The Enchantments from the ItemStack
     */
    public Map<Enchantment, Integer> getEnchantments() {
        return meta.hasEnchants() ? meta.getEnchants() : new HashMap<>();
    }

    /**
     * @return The Item Flags from the ItemStack
     */
    public Set<ItemFlag> getItemFlags() {
        return meta.getItemFlags().isEmpty() ? new HashSet<>() : meta.getItemFlags();
    }

    /**
     * @return A List of Patterns from the Banner
     */
    public List<Pattern> getPatterns() {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            return ((BannerMeta) meta).getPatterns();
        }

        return null;
    }

    /**
     * Set the Banner patterns
     *
     * @param patterns The patterns for the Banner
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setPatterns(final List<Pattern> patterns) {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            final BannerMeta bannerMeta = (BannerMeta) meta;

            bannerMeta.setPatterns(patterns);
            item.setItemMeta(bannerMeta);
        }

        return this;
    }

    /**
     * Set the Banner patterns
     *
     * @param patterns The patterns for the Banner
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setPatterns(final Pattern... patterns) {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            final BannerMeta bannerMeta = (BannerMeta) meta;

            bannerMeta.setPatterns(Arrays.asList(patterns));
            item.setItemMeta(bannerMeta);
        }

        return this;
    }

    /**
     * @param position The position from the Banner
     * @return A Pattern of a specific position
     */
    public Pattern getPattern(int position) {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            return ((BannerMeta) meta).getPattern(position);
        }

        return null;
    }

    /**
     * @return The Count of Patterns from the Banner
     */
    public int getPatternCount() {
        if (item.getType().name().contains("BANNER") && !item.getType().name().contains("PATTERN")) {
            return ((BannerMeta) meta).numberOfPatterns();
        }

        return 0;
    }

    /**
     * @return The Color from the Leather Armor
     */
    public Color getLeatherArmorColor() {
        return item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_HELMET ? ((LeatherArmorMeta) meta).getColor() : null;
    }

    /**
     * Set a Color for the Leather Armor
     *
     * @param color The Color for the Leather Armor
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setLeatherArmorColor(final Color color) {
        if (item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_HELMET) {
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;

            leatherArmorMeta.setColor(color);
            item.setItemMeta(leatherArmorMeta);
        }

        return this;
    }

    /**
     * @return The book title
     */
    public String getBookTitle() {
        return item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK ? ((BookMeta) meta).getTitle() : "";
    }

    /**
     * Set the book title
     *
     * @param title The title for the book
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setBookTitle(final String title) {
        if (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK) {
            final BookMeta bookMeta = (BookMeta) meta;

            bookMeta.setTitle(title);
            item.setItemMeta(bookMeta);
        }
        return this;
    }

    /**
     * @return The book author
     */
    public String getBookAuthor() {
        return item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK ? ((BookMeta) meta).getAuthor() : "";
    }

    /**
     * Set the book author
     *
     * @param author The author of the book
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setBookAuthor(final String author) {
        if (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK) {

            final BookMeta bookMeta = (BookMeta) meta;

            bookMeta.setAuthor(author);
            item.setItemMeta(bookMeta);
        }
        return this;
    }

    /**
     * @return The book pages
     */
    public List<Component> getBookPages() {
        return item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK ? ((BookMeta) meta).pages() : new ArrayList<>();
    }

    /**
     * Set the book pages
     *
     * @param pages The book pages
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setBookPages(final List<Component> pages) {
        if (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK) {
            final BookMeta bookMeta = (BookMeta) meta;

            bookMeta.addPages();
            item.setItemMeta(bookMeta);
        }
        return this;
    }

    /**
     * Set the book pages
     *
     * @param pages The book pages
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setBookPages(final Component... pages) {
        if (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK)
            setBookPages(Arrays.asList(pages));

        return this;
    }

    /**
     * @return The book page count
     */
    public int getBookPageCount() {
        return item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK ? ((BookMeta) meta).getPageCount() : 0;
    }

    /**
     * @return The list with all the potion effects
     */
    public List<PotionEffect> getPotionEffects() {
        return item.getType() == Material.POTION ? ((PotionMeta) meta).getCustomEffects() : new ArrayList<>();
    }

    /**
     * @param type The potion type
     * @return If the potion has a specify effect
     */
    public boolean hasPotionEffect(final PotionEffectType type) {
        return item.getType() == Material.POTION && ((PotionMeta) meta).hasCustomEffect(type);
    }

    /**
     * @return If the map scaling enabled
     */
    public boolean isMapScaling() {
        return item.getType() == Material.MAP && ((MapMeta) meta).isScaling();
    }

    /**
     * Set the map scaling
     *
     * @param scaling The scaling value
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setMapScaling(final boolean scaling) {
        if (item.getType() == Material.MAP) {
            final MapMeta mapMeta = (MapMeta) meta;

            mapMeta.setScaling(scaling);
            item.setItemMeta(mapMeta);
        }

        return this;
    }

    /**
     * @return If the ItemStack Unbreakable
     */
    public boolean isUnbreakable() {
        return meta.isUnbreakable();
    }

    /**
     * Make the Item Unbreakable
     *
     * @param unbreakable make the ItemStack Unbreakable or not
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setUnbreakable(final boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Repair the ItemStack
     *
     * @return {@link ItemBuilder}
     */
    public ItemBuilder repair() {
        if (item.getType().getMaxDurability() > 0)
            item.setDurability((short) 0);
        return this;
    }

    /**
     * Build the ItemBuilder to a ItemStack
     *
     * @return {@link org.bukkit.inventory.ItemStack}
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}