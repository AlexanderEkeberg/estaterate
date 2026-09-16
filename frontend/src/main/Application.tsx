import React from "react";

export function Application() {
  return (
    <>
      <div className="flex flex-col justify-center items-center h-screen text-white gap-2">
        <h1 className="bold-font text-2xl">Paste your URL</h1>
        <input
          placeholder="Ex. https://www.finn.no/realestate/homes/ad.html?finnkode=476389511"
          className="w-130 h-10 text-sm bg-[#10141E] text-white placeholder:text-gray-500 border border-gray-600 rounded-md py-2 px-4 focus:outline-none"
        ></input>
      </div>
    </>
  );
}
