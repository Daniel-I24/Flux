/** Same shape as T but every leaf is a plain string (used to type other locales). */
export type DeepStrings<T> = {
  [K in keyof T]: T[K] extends string ? string : DeepStrings<T[K]>;
};

/** Dotted paths to every string leaf: 'watch.saved', 'errors.NETWORK', ... */
export type LeafPaths<T> = {
  [K in keyof T & string]: T[K] extends string ? K : `${K}.${LeafPaths<T[K]>}`;
}[keyof T & string];

export type TranslationParams = Record<string, string | number>;
