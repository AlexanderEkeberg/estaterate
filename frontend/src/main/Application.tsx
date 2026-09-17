import React, { useState } from "react";
import { scrapeProperty } from "../api/propertyApi.js";

export function Application() {

    const [url, setUrl] = useState("");

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        try {
            await scrapeProperty(url);
            console.log("Url submitted:", url);
        } catch (error) {
            console.error(error);
        }
    }
  return (
    <>
      <div className="flex flex-col justify-center items-center h-screen text-white gap-2">
        <h1 className="bold-font text-2xl">Paste your URL</h1>
        <form onSubmit={handleSubmit} className="flex flex-col items-center gap-2">
          <input
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="Ex. https://www.finn.no/realestate/homes/ad.html?finnkode=476389511"
            className="w-130 h-10 text-sm bg-[#10141E] text-white placeholder:text-gray-500 border border-gray-600 rounded-md py-2 px-4 focus:outline-none"
          ></input>
          <button type="submit" className="bg-[#10141E]">Submit</button>
        </form>
      </div>
    </>
  );
}
